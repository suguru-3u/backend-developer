# 第18章　非正規化とストリームJOIN ― 複数テーブルを1ドキュメントに

> Elasticsearch学習教科書 ― フェーズ3「CDCパイプライン構築」
> 前提知識：第13〜17章
> 付属ファイル：`phase3-18/01-categories.sql`、`phase3-18/logstash/config/pipelines.yml`、`phase3-18/logstash/pipeline/products.conf`、`phase3-18/logstash/pipeline/categories.conf`

ここまでの `products` テーブルは、意図的に「1テーブルで完結する」形にしてありました。`tags` をカンマ区切りの文字列にしていたのも、`category` にコード文字列を直接持たせていたのも、そのためです。

現実は違います。DBは正規化されています。そして **CDCはテーブル単位でしかイベントを出しません**。この章は、CDC × 検索インデックスにおける最大の設計課題を扱います。

---

## この章で学ぶこと（学習目標）

- 「テーブル単位のイベント」と「1ドキュメント」のギャップがなぜ難しいのかを説明できる
- **親の1行更新が子の何千件更新になる**（fan-out）問題を説明できる
- 非正規化の4つのアプローチを比較し、状況に応じて選べる
- Elasticsearch の **enrich プロセッサ**でカテゴリ名を自動付与できる
- 参照元が変わったときの**追従（再enrich）** の手順を実行できる
- **Outbox パターン**が、なぜ検索インデックス構築の実務的な最適解になりやすいのかを説明できる

---

## 18.1 問題の全体像

正規化されたスキーマを追加します。

```bash
docker compose exec -T mysql mysql -uroot -prootpw shopdb < phase3-18/01-categories.sql
```

```
【MySQL（正規化）】                     【Elasticsearch（非正規化）】

  products                              products インデックス
   id, name, price, category ──┐         {
                               │           "name": "軽量ノートパソコン 14インチ",
  categories                   │           "price": 79000,
   code, name, display_order ◄─┘           "category": "pc",
                                           "category_name": "パソコン",  ← categories から
                                           "tags": ["軽量","14インチ"]
                                         }
```

ESで「パソコンカテゴリの商品」と日本語で検索したり、カテゴリ名でファセットを出したりするには、**`category_name` が商品ドキュメントの中に入っている必要があります**。ESは検索時のJOINが（ほぼ）できないからです。

そしてCDCは、`products` と `categories` を**別々のトピック**に流します。

```
  shop.shopdb.products    ──►  ?
                                 └─► 1つの products ドキュメント
  shop.shopdb.categories  ──►  ?
```

この「?」をどう設計するか、が本章のテーマです。

### 本当の難所は「親の更新」

商品側のイベントが来たときにカテゴリ名を引く、というだけなら難しくありません。**難しいのは逆方向です。**

```sql
UPDATE categories SET name = 'PC・ノートパソコン' WHERE code = 'pc';
```

このSQLは **1行の更新**です。しかしESでは、

```
  category = 'pc' の商品ドキュメント全部（3,000件かもしれない）を
  category_name = 'PC・ノートパソコン' に書き換えなければならない
```

**1イベント → N件更新**。これを **fan-out（ファンアウト）** と呼びます。カテゴリのような「親」の変更は頻度こそ低いものの、1回の影響範囲が巨大です。しかも「更新中の商品」と「更新済みの商品」が混在する時間が生まれます。

CDCで検索インデックスを作るとき、設計者が本当に考えるべきなのはここです。

---

## 18.2 4つのアプローチ

| | A. ソースDB側で解決 | B. Outboxパターン | C. ストリーム処理でJOIN | D. ES側で解決 |
|---|---|---|---|---|
| やり方 | 非正規化済みのテーブル／ビューを用意し、それをCDC対象にする | アプリがトランザクション内で**完成形のJSON**を`outbox`に書き、それを流す | Kafka Streams / Flink / ksqlDB で複数トピックをJOINして新トピックを作る | ESの `enrich` や `nested`、アプリ側JOINで寄せる |
| 実装場所 | DB | アプリケーション | ストリーム基盤 | Elasticsearch |
| 親更新のfan-out | DBのトリガ等で発生 | アプリが責任を持つ | 基盤が自動で再送 | 再enrich＋`_update_by_query` |
| 長所 | 既存資産で完結 | **意図が明確／ドメイン知識をアプリに置ける** | 汎用的・強力 | 追加コンポーネント不要 |
| 短所 | DBに負荷と複雑さ | アプリ改修が必要（＝CDCの非侵襲性を一部手放す） | **運用対象が増える**（学習コストも大） | 参照元の更新追従を自前で回す必要 |
| 向く規模 | 小 | 中〜大 | 大 | 小〜中 |

**本章では D を実装します。** 追加コンポーネントなしで、いまの環境のまま試せるからです。そのうえで B（Outbox）を 18.6 で扱います。**実務で最も採用されているのは B** で、理由もそこで説明します。

> 💡 **コラム：ESの `nested` と `join` は今回の答えにならない**
> ESにも親子関係を扱う機能はありますが、性質が違います。
> - **`nested`**：1ドキュメントの中に配列オブジェクトを埋め込む。「商品の中のレビュー配列」のような**所有関係**向け。別テーブルの更新に追従する仕組みではない
> - **`join` フィールド（親子）**：親と子を別ドキュメントとして同一シャードに置ける。更新は独立してできるが、**検索性能が大きく落ち**、集計にも制約が出る
> どちらも「マスタの値を各ドキュメントにコピーする」用途には向きません。検索エンジンでは**コピーして持つ（非正規化）** が基本方針です。

---

## 18.3 ハンズオン ― categories もCDCに乗せる

まずカテゴリマスタをESに持ち込みます。

### ① Debezium の対象テーブルを増やす

```bash
curl -s -X PUT -H "Content-Type: application/json" \
  http://localhost:8083/connectors/products-source/config -d '{
  "connector.class": "io.debezium.connector.mysql.MySqlConnector",
  "tasks.max": "1",
  "database.hostname": "mysql",
  "database.port": "3306",
  "database.user": "debezium",
  "database.password": "dbzpw",
  "driver.allowPublicKeyRetrieval": "true",
  "database.server.id": "184054",
  "topic.prefix": "shop",
  "database.include.list": "shopdb",
  "table.include.list": "shopdb.products,shopdb.categories",
  "schema.history.internal.kafka.bootstrap.servers": "kafka:9092",
  "schema.history.internal.kafka.topic": "schema-history.shop",
  "snapshot.mode": "initial",
  "include.schema.changes": "false",
  "key.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "key.converter.schemas.enable": "false",
  "value.converter.schemas.enable": "false",
  "transforms": "unwrap",
  "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
  "transforms.unwrap.delete.tombstone.handling.mode": "rewrite",
  "transforms.unwrap.add.fields": "op,source.ts_ms,source.file,source.pos"
}' | jq -r '.name'
```

> ⚠️ **後から対象テーブルを増やしても、そのテーブルのスナップショットは自動では走りません。** コネクタは「もう初期スナップショットは済んだ」と記録しているためです。既存行を流し込むには、第17章で学んだ**増分スナップショット**（シグナルテーブルに `execute-snapshot` を1行INSERT）を使うか、環境を作り直します。学習中は作り直しが手軽です。

### ② Logstash のパイプラインを2本に分ける

ここに、Logstashで**必ず一度は踏む落とし穴**があります。

> **`pipeline/` ディレクトリに `.conf` を2つ置くと、Logstash はそれらを「1本のパイプラインとして連結」します。**

つまり `products.conf` と `categories.conf` を並べただけだと、**両方の input が両方の output に流れ込み**、カテゴリのデータが products インデックスに書き込まれます。防ぐには `pipelines.yml` で明示的に分離します。

```yaml
# phase3-18/logstash/config/pipelines.yml
- pipeline.id: products
  path.config: "/usr/share/logstash/pipeline/products.conf"

- pipeline.id: categories
  path.config: "/usr/share/logstash/pipeline/categories.conf"
```

compose の logstash サービスにマウントを1行足します。

```yaml
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline:ro
      - ./logstash/config/pipelines.yml:/usr/share/logstash/config/pipelines.yml:ro
```

### ③ categories インデックスを作る

```bash
curl -X PUT "localhost:9200/categories" -H 'Content-Type: application/json' -d '
{
  "mappings": {
    "dynamic": "false",
    "properties": {
      "code":          { "type": "keyword" },
      "name":          { "type": "keyword" },
      "display_order": { "type": "integer" }
    }
  }
}'

docker compose restart logstash
sleep 30
curl -s "localhost:9200/categories/_search?pretty&size=10&_source=code,name"
```

4件のカテゴリが入っていれば成功です。**マスタテーブルもCDCで同期されている**状態になりました。

---

## 18.4 ハンズオン ― enrich プロセッサでカテゴリ名を付与する

Elasticsearch には、**インデックスする直前に別インデックスを参照して値を埋め込む** `enrich` プロセッサがあります。SQLでいう「取り込み時にJOINしておく」処理です。

```
   Logstash                Elasticsearch
   ────────►  [ ingest pipeline ]  ────►  products_v3
   {                    │
     "category":"pc"    │  enrich: categories を code で引く
   }                    │  set:    category_name = "パソコン"
                        ▼
                     {"category":"pc","category_name":"パソコン", ...}
```

### ① enrich ポリシーを作って実行する

```bash
curl -X PUT "localhost:9200/_enrich/policy/category-lookup" \
  -H 'Content-Type: application/json' -d '
{
  "match": {
    "indices": "categories",
    "match_field": "code",
    "enrich_fields": ["name", "display_order"]
  }
}'

# ポリシーの実行 ＝ 参照用のスナップショットを作る操作
curl -X POST "localhost:9200/_enrich/policy/category-lookup/_execute?wait_for_completion=true"
```

> 📌 **ここが最重要ポイント：`_execute` は「その時点の categories のコピー」を作ります。**
> enrich は検索のたびに `categories` を見にいくのではなく、**専用の内部インデックス（スナップショット）** を参照します。だから高速なのですが、裏を返すと **`categories` を更新しても、ポリシーを再実行するまで反映されません**。これが 18.5 の話につながります。

### ② ingest パイプラインを作る

```bash
curl -X PUT "localhost:9200/_ingest/pipeline/products-enrich" \
  -H 'Content-Type: application/json' -d '
{
  "description": "商品ドキュメントにカテゴリ名を埋め込む",
  "processors": [
    {
      "enrich": {
        "policy_name": "category-lookup",
        "field": "category",
        "target_field": "_cat",
        "max_matches": 1,
        "ignore_missing": true
      }
    },
    {
      "set": {
        "field": "category_name",
        "copy_from": "_cat.name",
        "ignore_empty_value": true
      }
    },
    { "remove": { "field": "_cat", "ignore_missing": true } }
  ]
}'
```

### ③ マッピングにフィールドを追加し、パイプラインを既定にする

```bash
curl -X PUT "localhost:9200/products_v3/_mapping" -H 'Content-Type: application/json' -d '
{ "properties": {
    "category_name": {
      "type": "text",
      "analyzer": "ja_index", "search_analyzer": "ja_search",
      "fields": { "keyword": { "type": "keyword" } }
    }
} }'

# このインデックスへの書き込みは、常にこのパイプラインを通す
curl -X PUT "localhost:9200/products_v3/_settings" -H 'Content-Type: application/json' -d '
{ "index.default_pipeline": "products-enrich" }'
```

`index.default_pipeline` を設定すると、**Logstash 側は何も変えなくても**、以後の書き込みがすべて enrich を通ります。パイプラインの責務がきれいに分かれます。

### ④ 既存ドキュメントにも適用する

```bash
curl -X POST "localhost:9200/products/_update_by_query?pipeline=products-enrich&conflicts=proceed&refresh"

curl -s "localhost:9200/products/_search?pretty&size=2&_source=name,category,category_name"
```

```json
{ "name": "軽量ノートパソコン 14インチ 極薄モデル",
  "category": "pc", "category_name": "パソコン" }
```

**カテゴリ名が商品ドキュメントに入りました。** これで「パソコン」という日本語でも、カテゴリ由来のヒットが得られます。

> ⚠️ `_update_by_query` は既定では ingest パイプラインを通しません。**`?pipeline=` を明示**する必要があります。忘れると「何も変わらない」と悩むことになります。

---

## 18.5 親が変わったとき ― fan-out への対処

いよいよ本丸です。カテゴリ名を変更してみます。

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb \
  -e "UPDATE categories SET name = 'PC・ノートパソコン' WHERE code = 'pc';"
sleep 3

curl -s "localhost:9200/categories/_doc/pc?pretty&_source=name"      # → 更新されている
curl -s "localhost:9200/products/_doc/1?pretty&_source=category_name" # → 古いまま！
```

`categories` インデックスは即座に更新されますが、**商品側の `category_name` は古いままです**。18.4 で説明したとおり、enrich は実行時点のスナップショットを見ているからです。

追従には**2ステップ**が必要です。

```bash
# ステップ1：enrichのスナップショットを作り直す
curl -X POST "localhost:9200/_enrich/policy/category-lookup/_execute?wait_for_completion=true"

# ステップ2：影響を受ける商品だけを再インデックスする
curl -X POST "localhost:9200/products/_update_by_query?pipeline=products-enrich&conflicts=proceed&refresh" \
  -H 'Content-Type: application/json' -d '
{ "query": { "term": { "category": "pc" } } }'

curl -s "localhost:9200/products/_doc/1?pretty&_source=category_name"  # → 新しい名前
```

**ステップ2でクエリを絞っている点が重要です。** 全件を再インデックスすると、商品数が多いときにクラスタへ大きな負荷がかかります。`term` クエリで「そのカテゴリの商品だけ」に限定すれば、影響範囲は最小になります。

### これをどう自動化するか

手で叩くわけにはいきません。実務での定番は次の3つです。

| 方式 | 概要 | 備考 |
|---|---|---|
| **カテゴリトピックの購読** | `shop.shopdb.categories` を購読する小さなアプリを作り、変更を検知したら上の2コマンドを実行する | 最も素直。Logstash の `http` output や小さなスクリプトで実現可能 |
| **定期実行** | 数分〜1時間ごとにポリシー再実行＋全件 `_update_by_query` | マスタの更新頻度が低ければ現実的。単純で壊れにくい |
| **上流で解決** | 18.6 の Outbox パターンなど、そもそも完成形イベントを流す | fan-out を上流の責務にする |

> 💡 **`conflicts=proceed` の意味**
> `_update_by_query` の実行中に、同じドキュメントがCDC経由で更新されるとバージョン衝突が起きます。既定では処理全体が失敗しますが、`conflicts=proceed` を付けると**衝突した分だけスキップして続行**します。CDCと併走する再インデックスでは、ほぼ必須のオプションです（スキップされた分は、CDC側の新しい値が入っているので問題ありません）。

---

## 18.6 Outbox パターン ― 実務での本命

第13章のコラムで名前だけ出した **Outbox パターン**を、ここで正面から扱います。

### 考え方

「CDCで生テーブルの変更を流す」のをやめ、**アプリケーションが『検索インデックスに入れたい完成形』をイベントとして書く**方式です。

```
  アプリのトランザクション
  ┌──────────────────────────────────────────┐
  │ UPDATE products SET price = 79000 ...     │
  │ UPDATE product_tags ...                   │
  │ INSERT INTO outbox (aggregateid, payload) │
  │   VALUES ('1', '{完成形のJSON}')           │  ← 同じトランザクション！
  └──────────────────────────────────────────┘
                     │ COMMIT
                     ▼
        Debezium は outbox テーブルだけを見る
                     ▼
        Kafka: products イベント（すでに非正規化済み）
                     ▼
        Elasticsearch（そのまま入れるだけ）
```

`payload` には、カテゴリ名もタグ配列も在庫状況も**すべて入った完成形のJSON**を入れます。

```json
{
  "id": 1,
  "name": "軽量ノートパソコン 14インチ 極薄モデル",
  "category": "pc",
  "category_name": "PC・ノートパソコン",
  "tags": ["軽量", "14インチ", "モバイル"],
  "price": 79000,
  "in_stock": true
}
```

### Debezium の EventRouter SMT

Debezium には Outbox 専用の変換が用意されています。

```json
  "table.include.list": "shopdb.outbox",
  "transforms": "outbox",
  "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
  "transforms.outbox.route.by.field": "aggregatetype",
  "transforms.outbox.table.field.event.key": "aggregateid",
  "transforms.outbox.table.field.event.payload": "payload"
```

これにより、
- `aggregatetype` の値ごとにトピックが振り分けられる（`outbox.event.product` など）
- **メッセージキーが `aggregateid`**（＝商品ID）になる → 第14章の順序保証がそのまま効く
- **メッセージの中身が `payload` そのもの**になる → シンク側は入れるだけ

### なぜ実務で選ばれるのか

| 論点 | Outboxパターンの答え |
|---|---|
| 二重書き込み問題（第13章） | **解決**。DBトランザクション内なので、業務更新とイベントが同時に成功／失敗する |
| 非正規化・JOIN | **アプリが行う**。ドメイン知識を持つ場所で組み立てるので、正しく作れる |
| fan-out（親の更新） | アプリが「カテゴリ名が変わったら該当商品のイベントを再発行する」と明示的に書ける |
| スキーマ変更の影響 | `payload` は**アプリが決めた契約**なので、テーブル定義の変更が直接下流を壊さない |
| 代償 | **アプリ改修が必要**。CDCの「非侵襲」という長所を一部手放す |

最後の代償が重要です。**Outbox は「CDCを使わない」のではなく、「CDCの対象を生テーブルからイベントテーブルに変える」設計**です。第13章で学んだCDCの利点（完全性・順序性・トランザクション整合）はすべて維持したまま、非正規化の責務をアプリに移せます。

**判断の目安**

- 既存システムに手を入れられない／まずは動かしたい → **生テーブルCDC ＋ enrich**（本章 18.4）
- これから作る／検索が事業の中核 → **Outbox パターン**
- 複数チームの多数のテーブルを横断し、リアルタイム性も要る → **Kafka Streams / Flink でのストリームJOIN**

---

## まとめ

- CDCは**テーブル単位**でイベントを出すが、Elasticsearchは**1ドキュメントに全部入っている**ことを好む。このギャップを埋めるのが非正規化設計
- 難所は「子」ではなく「**親**」。カテゴリ名の1行更新が、何千件の商品ドキュメント更新（**fan-out**）になる
- アプローチは4つ：**A.ソースDB側**、**B.Outboxパターン**、**C.ストリーム処理でJOIN**、**D.ES側（enrich等）**
- ESの `nested` / `join` は所有関係や親子検索のための機能であり、**マスタ値のコピーには向かない**。検索エンジンでは非正規化が基本
- **enrich プロセッサ**は「取り込み時JOIN」。`_enrich/policy` を作って `_execute` し、ingest パイプラインから呼ぶ。`index.default_pipeline` にすれば書き込み側の変更は不要
- **enrich は実行時点のスナップショットを参照する。** 参照元が変わったら「ポリシー再実行」＋「影響範囲を絞った `_update_by_query`」の2ステップが必要（`?pipeline=` と `conflicts=proceed` を忘れない）
- Logstash で複数の入出力を扱うときは、**`pipelines.yml` でパイプラインを分離する**。同じディレクトリの `.conf` は連結されてしまう
- **Outbox パターン**は、アプリがトランザクション内で完成形JSONを書き、Debezium の `EventRouter` SMT でそれを配信する方式。二重書き込み問題を解決しつつ、非正規化とfan-outの責務をドメイン層に置ける。実務での本命

---

## 理解度チェック

**問1.** `UPDATE categories SET name = '...' WHERE code = 'pc';` という1行の更新が、Elasticsearch側では大仕事になります。なぜですか。この現象を何と呼びますか。

**問2.** enrich プロセッサを設定したのに、`categories` を更新しても商品ドキュメントの `category_name` が変わりません。原因と、必要な2つの操作を答えてください。

**問3.** `_update_by_query` を実行したのに、ドキュメントに `category_name` が付きませんでした。設定は正しいはずです。何を見落としていますか。

**問4.** Logstash の `pipeline/` ディレクトリに `products.conf` と `categories.conf` を置いたところ、`products` インデックスにカテゴリのデータが混入しました。なぜですか。どう直しますか。

**問5.** Outbox パターンが「二重書き込み問題（第13章）」を解決できるのはなぜですか。また、この方式で手放すことになるCDCの長所は何ですか。

<details>
<summary>解答を見る</summary>

**問1.**
Elasticsearch は検索時にテーブルJOINができないため、カテゴリ名を各商品ドキュメントに**コピーして持たせる（非正規化する）** 必要がある。したがって親（カテゴリ）を1行更新すると、そのカテゴリに属する**すべての商品ドキュメント**を書き換えなければならない。
この「1イベント → N件更新」を **fan-out（ファンアウト）** と呼ぶ。

**問2.**
原因：enrich プロセッサは `categories` インデックスを直接参照しているのではなく、**ポリシー実行（`_execute`）時点で作られたスナップショット**を参照しているため。参照元を更新しても自動では反映されない。
必要な操作：
1. `POST /_enrich/policy/category-lookup/_execute` でスナップショットを作り直す
2. `POST /products/_update_by_query?pipeline=products-enrich` で、影響を受ける商品ドキュメントを再インデックスする（`{"query":{"term":{"category":"pc"}}}` のように範囲を絞るのが望ましい）

**問3.**
`_update_by_query` は**既定ではingestパイプラインを通らない**。`index.default_pipeline` を設定していても適用されないため、`?pipeline=products-enrich` をクエリパラメータで明示する必要がある。

**問4.**
Logstash は `pipeline/` 配下の `.conf` ファイルを**すべて連結して1本のパイプラインとして扱う**ため、両方の input が両方の output に流れ込んだ。
`pipelines.yml` に `pipeline.id` と `path.config` の組を書いて**明示的に分離**し、そのファイルを `/usr/share/logstash/config/pipelines.yml` にマウントする。

**問5.**
アプリケーションが**業務テーブルの更新と `outbox` への書き込みを同一のDBトランザクションで行う**ため。両方成功か両方失敗しかなく、「DBは更新されたがイベントは出ていない」という不整合が原理的に起きない。イベントの配信自体はCDC（Debezium）が担うので、順序性も完全性も維持される。
手放すもの：**非侵襲性**。生テーブルをそのまま流す方式と違い、アプリケーションの改修（トランザクション内でのoutbox書き込み、完成形JSONの組み立て）が必要になる。

</details>

---

## 次の章へ

これでパイプラインは、機能としては完成しました。最終章では **「これを人に任せられる状態にする」** ことを扱います。

何を監視すれば異常に気づけるのか。壊れたときにどの順で調べるのか。学習用に無効化してきたセキュリティをどう戻すのか。そして、フェーズ1の転置インデックスから始まった長い道のりを、1本の総合演習で振り返ります。

> **次章：第19章「運用と総合演習 ― 監視・障害対応・本番化チェックリスト」**

---

### この章のキーワード

非正規化 / 正規化 / fan-out / ストリームJOIN / enrich プロセッサ / enrich ポリシー / `_enrich/policy` / `_execute`（スナップショット）/ ingest パイプライン / `index.default_pipeline` / `_update_by_query?pipeline=` / `conflicts=proceed` / `nested` / `join` フィールド / Logstash `pipelines.yml` / Outbox パターン / `EventRouter` SMT / `aggregatetype` / `aggregateid` / Kafka Streams / Flink / ksqlDB
