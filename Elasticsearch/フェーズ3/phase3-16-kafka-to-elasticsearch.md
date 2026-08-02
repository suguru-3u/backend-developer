# 第16章　Kafka から Elasticsearch へ ― シンク構築と tombstone による削除

> Elasticsearch学習教科書 ― フェーズ3「CDCパイプライン構築」
> 前提知識：第13〜15章
> 付属ファイル：`phase3-16/docker-compose.yml`、`phase3-16/products-index.json`、`phase3-16/register-mysql-source-v2.json`、`phase3-16/logstash/pipeline/products.conf`、`phase3-16/register-es-sink.json`

前章で、MySQL の変更が Kafka まで届きました。残るは最後の一区間です。

しかし、この区間は「つなぐだけ」では終わりません。**Kafka を流れているイベントの形と、Elasticsearch に入れたいドキュメントの形が違う**からです。

```
【Kafkaに流れているもの】                【ESに入れたいもの】
{                                       {
  "before": {...},                        "name": "軽量ノートパソコン 14インチ",
  "after": {                              "category": "pc",
    "id": 1,                              "tags": ["軽量","14インチ","モバイル"],  ← 配列
    "tags": "軽量,14インチ,モバイル",        "price": 89000,
    "in_stock": 1,                        "in_stock": true                        ← 真偽値
    ...                                 }
  },                                      _id = "1"                               ← 主キー
  "op": "u", "source": {...}
}
```

この「形の違い」を埋める作業が、この章の中身です。

---

## この章で学ぶこと（学習目標）

この章を読み終えると、次のことができるようになります。

- Kafka → Elasticsearch の実現手段を3つ挙げ、それぞれの向き不向きを説明できる
- **SMT（Single Message Transform）** でDebeziumのイベントを平坦化（unwrap）できる
- `delete.tombstone.handling.mode` の各モードの違いと選び方を説明できる
- ドキュメント `_id` に主キーを使うことが、なぜ冪等性の要なのかを説明できる
- Logstash で Kafka → Elasticsearch のパイプラインを構築し、**INSERT / UPDATE / DELETE がESに反映される**ところまで完走できる
- Kafka Connect の Elasticsearch シンクコネクタの主要設定と **DLQ（Dead Letter Queue）** を説明できる

---

## 16.1 出口の選択肢

Kafka から Elasticsearch へデータを運ぶ方法は、大きく3つあります。

| | ①Kafka Connect ESシンク | ②Logstash | ③自作コンシューマ |
|---|---|---|---|
| 実体 | Connect上で動くプラグイン | Elastic公式のデータ処理ツール | 自分で書くアプリ |
| 設定 | JSONをREST APIにPOST | 独自DSLの設定ファイル | コード |
| 変換の柔軟性 | SMTの範囲内 | **非常に高い**（条件分岐・Ruby） | 無制限 |
| DLQ | **標準機能あり** | 自前で工夫が必要 | 自前 |
| ESとの版の整合 | プラグイン側の対応に依存 | **ESと同じ版が出る** | クライアント次第 |
| 運用対象 | Connectに相乗り | プロセスが1つ増える | アプリが1つ増える |

**本章では ② Logstash を主経路として構築します。** 理由は2つです。

1. **バージョンの確実性**：Logstash は Elasticsearch と同じバージョン（9.4.3）が提供されており、確実に噛み合います。一方、OSSのESシンクコネクタは、2026年8月現在、公式ドキュメント上の対応表記が **Elasticsearch 8.x まで**にとどまっています（9.x で動く可能性は十分ありますが、教科書の手順が止まるリスクは避けたい）
2. **変換の学習になる**：今回必要な「カンマ区切り→配列」「1→true」「削除の振り分け」を、条件分岐として素直に書けます

ただし **①Kafka Connect ESシンクは実務で最も広く使われる構成**なので、16.6 で設定と設計論点をきちんと扱います。どちらを選んでも、**設計上考えることは同じ**です。

> 💡 **コラム：ESシンクコネクタのライセンスに注意**
> Confluent の `kafka-connect-elasticsearch` は途中でライセンスが Confluent Community License に変更されています（自社利用は可、競合SaaSとしての提供は不可）。Apache 2.0 のまま使いたい場合は、ライセンス変更前にフォークされた **Aiven 版**（`Aiven-Open/elasticsearch-connector-for-apache-kafka`）という選択肢があります。OSSを業務で使う際は、こうしたライセンス条件の確認も設計判断の一部です。

---

## 16.2 準備 ― Elasticsearch 側のインデックスを先に作る

**CDCを始める前に、必ずマッピングを明示的に作ってください。** 動的マッピングに任せると、最初に届いたドキュメントの値から型が推測され、あとから直せなくなります（第3章）。

一度消してから作り直します。

```bash
curl -X DELETE "localhost:9200/products"

curl -X PUT "localhost:9200/products" \
  -H "Content-Type: application/json" \
  --data @phase3-16/products-index.json
```

`products-index.json` の要点だけ抜き出します。

```json
{
  "mappings": {
    "dynamic": "false",
    "properties": {
      "id":          { "type": "integer" },
      "name":        { "type": "text", "analyzer": "ja_index",
                       "search_analyzer": "ja_search" },
      "category":    { "type": "keyword" },
      "tags":        { "type": "keyword" },
      "price":       { "type": "integer" },
      "in_stock":    { "type": "boolean" },
      "updated_at":  { "type": "date" }
    }
  }
}
```

アナライザーの定義はフェーズ2（第9・10章）で作ったものと同じ考え方です。日本語検索の資産はそのまま活かせます。

**`"dynamic": "false"` に注目してください。** これは「マッピングに定義していないフィールドが来ても、エラーにせず、インデックス（検索対象化）もしない」という設定です。CDCでは上流から予期しないフィールドが流れてくることがあるため、この保険が効きます。選択肢は3つあります。

| 設定 | 未知のフィールドが来たら |
|---|---|
| `true`（既定） | 型を推測して自動でマッピングに追加する（**事故のもと**） |
| `false` | `_source` には残るが、検索対象にはならない（**今回はこれ**） |
| `strict` | **エラーにして拒否する**（厳格。第17章で再検討します） |

---

## 16.3 SMT ― Debezium のイベントを平坦化する

Kafka を流れているのは `before` / `after` / `source` / `op` を含む**入れ子構造**です。しかし ES に入れたいのは `after` の中身だけ。この変換を、**SMT（Single Message Transform）** で行います。

SMTは「コネクタが1件ずつメッセージを加工する小さな部品」で、ソース側でもシンク側でも使えます。Debezium が提供する `ExtractNewRecordState`（通称 unwrap）が、まさにこの用途のためのものです。

`register-mysql-source-v2.json` で、第15章の設定に4行だけ足します。

```json
    "transforms": "unwrap",
    "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
    "transforms.unwrap.delete.tombstone.handling.mode": "rewrite",
    "transforms.unwrap.add.fields": "op,source.ts_ms,source.file,source.pos"
```

これで、流れるメッセージがこう変わります。

```
【before】                          【after（unwrap適用後）】
{                                   {
  "before": null,                     "id": 1,
  "after": {                          "name": "軽量ノートパソコン 14インチ",
    "id": 1,                          "tags": "軽量,14インチ,モバイル",
    "name": "...",         ────►      "price": 89000,
    "price": 89000                    "in_stock": 1,
  },                                  "__op": "u",
  "op": "u",                          "__source_ts_ms": 1785312000100,
  "source": {...}                     "__source_file": "binlog.000002",
}                                     "__source_pos": 2841
                                    }
```

`add.fields` で指定したメタデータは `__` 付きで残ります。**第17章で「古いイベントによる上書き」を防ぐときに、この `__source_ts_ms` などを使います**。今は残しておくだけです。

### 削除をどう表現するか ― `delete.tombstone.handling.mode`

ここが設計の分かれ目です。unwrap は既定では**削除イベントを捨ててしまう**ため、必ず明示的に指定します。

| モード | 削除時に下流へ届くもの | 向いている下流 |
|---|---|---|
| `drop`（既定） | **何も届かない**（削除が反映されない！） | ― |
| `tombstone` | 値が `null` のメッセージのみ | Kafka Connect ESシンク |
| `rewrite` | 通常のメッセージ＋`__deleted: "true"` | **Logstash・自作アプリ** |
| `rewrite-with-tombstone` | 上記の両方 | 併用したいとき |

Logstash の Kafka入力は「値が `null` のメッセージ」を素直に扱えないため、**`rewrite` を選びます**。削除が「`__deleted: "true"` という目印の付いた普通のメッセージ」として届くので、条件分岐で振り分けられます。

> ⚠️ `drop.tombstones` と `delete.handling.mode` という似た名前の古い設定がネット上に大量にありますが、**Debezium 3.2 で削除されました**。現行は `delete.tombstone.handling.mode` の1つだけです。

### 設定を入れ替える

すでに登録済みのコネクタは、設定部分だけをPUTで更新できます。

```bash
# config の中身だけ（"name" と "config" の入れ子を外したもの）をPUTする
curl -s -X PUT -H "Content-Type: application/json" \
  --data "$(jq '.config' phase3-16/register-mysql-source-v2.json)" \
  http://localhost:8083/connectors/products-source/config | jq '.name'
```

> 学習中は、`docker compose down && docker compose up -d --build` で環境ごと作り直し、第15章の手順（履歴トピック作成 → コネクタ登録）を `register-mysql-source-v2.json` でやり直すほうが確実です。以降の説明はその前提で進めます。

---

## 16.4 ハンズオン ― Logstash でつなぐ

### パイプラインを書く

`phase3-16/logstash/pipeline/products.conf` の中身を、3つのブロックで読み解きます。

**input：Kafkaから読む**

```
input {
  kafka {
    bootstrap_servers => "kafka:9092"
    topics            => ["shop.shopdb.products"]
    group_id          => "logstash-es-sink"
    auto_offset_reset => "earliest"
    codec             => json
    ecs_compatibility => disabled
  }
}
```

第14章で学んだコンシューマグループそのものです。`group_id` を指定しているので、Logstash を再起動しても**続きから**読みます。`auto_offset_reset => "earliest"` は「グループの記録がない初回だけ、先頭から読む」という意味です。

**filter：形を整える**

```
  # 1. 削除か、追加/更新かを振り分ける
  if [__deleted] == "true" {
    mutate { add_field => { "[@metadata][action]" => "delete" } }
  } else {
    mutate { add_field => { "[@metadata][action]" => "index" } }
  }

  # 2. MySQLの型 → Elasticsearchの型
  mutate { convert => { "in_stock" => "boolean" } }
  if [tags] and [tags] != "" {
    mutate { split => { "tags" => "," } }
  }

  # 3. CDCのメタデータを落とす
  mutate { remove_field => ["@version", "event",
                            "__deleted", "__op",
                            "__source_ts_ms", "__source_file", "__source_pos"] }
```

- `[@metadata]` に入れた項目は**ESには送られません**。処理の途中で使う一時変数として使えます
- `in_stock` は MySQL の `BOOLEAN`（実体は `TINYINT(1)`）なので `1` / `0` で届きます。`convert` で真偽値にします
- `tags` のカンマ区切り文字列を `split` で配列にします。これで ES 側の `keyword` 配列と噛み合います

**output：Elasticsearchへ書く**

```
output {
  elasticsearch {
    hosts           => ["http://elasticsearch:9200"]
    index           => "products"
    document_id     => "%{[id]}"
    action          => "%{[@metadata][action]}"
    manage_template => false
  }
}
```

3つの重要な指定があります。

**`document_id => "%{[id]}"` ― この章で最も重要な1行です。**
第13章の「論点1：冪等性」がここで回収されます。ESは `_id` を指定した書き込みを「同じIDなら上書き」として扱うため、**同じイベントが2回届いても結果が変わりません**。Kafka は at-least-once なので、この指定がないと再送のたびに重複ドキュメントが増殖します。

**`action => "%{[@metadata][action]}"`**
filter で振り分けた値がそのまま入り、削除イベントのときだけ `DELETE /products/_doc/7` が発行されます。

**`manage_template => false`**
Logstash は既定で自作のインデックステンプレートを登録しようとします。**これを止めないと、16.2 で丁寧に作ったkuromojiのマッピングが台無しになります。** 必ず指定してください。

### 起動する

```bash
docker compose up -d
docker compose logs -f logstash
```

`Pipeline started` のようなログが出れば動いています（起動には30秒ほどかかります）。

### 確認する

```bash
curl -s "localhost:9200/products/_count?pretty"
curl -s "localhost:9200/products/_doc/1?pretty"
```

```json
{
  "_index": "products",
  "_id": "1",
  "_source": {
    "id": 1,
    "name": "軽量ノートパソコン 14インチ",
    "category": "pc",
    "tags": ["軽量", "14インチ", "モバイル"],
    "price": 98000,
    "in_stock": true,
    "updated_at": 1785312000000,
    "@timestamp": "2026-08-03T09:20:00.000Z"
  }
}
```

**`tags` が配列に、`in_stock` が `true` になっています。** `_id` が `"1"` ＝ MySQLの主キーであることも確認してください。

> 📌 `@timestamp` は Logstash が自動で付ける項目です。`_source` には残りますが、マッピングを `dynamic: false` にしているので**検索対象にはなりません**。害はないので、そのままにしておきます。
> `updated_at` が数値なのは、MySQL の `DATETIME` を Debezium がエポックミリ秒に変換するためです。ES の `date` 型はエポックミリ秒も受け付けるので、日付として正しく扱われます。

---

## 16.5 ハンズオン ― CRUDが伝わることを確認する

いよいよ、**フェーズ1から通しての集大成**です。MySQL を触るだけで、日本語検索の結果が変わることを確認します。

### 更新（UPDATE）

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb \
  -e "UPDATE products SET price = 79000, name = '軽量ノートパソコン 14インチ 極薄モデル' WHERE id = 1;"

sleep 3
curl -s "localhost:9200/products/_doc/1?pretty" | grep -E '"name"|"price"'
```

数秒で新しい名前と価格に変わります。しかも `_id` は同じ `1` のままで、**ドキュメントが増えていません**（冪等性）。

### 検索してみる（フェーズ2の資産が効いていることの確認）

```bash
curl -s "localhost:9200/products/_search?pretty" -H 'Content-Type: application/json' -d '
{
  "query": { "match": { "name": "パソコン" } },
  "_source": ["name","price","in_stock"]
}'
```

シノニム設定により「PC」や「コンピュータ」でも同じ商品がヒットするはずです。**フェーズ2で作った検索品質の作り込みが、CDCで自動供給されるデータの上でそのまま動いています。**

### 追加（INSERT）

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb -e "
INSERT INTO products (name, description, category, tags, price, in_stock)
VALUES ('ノイズキャンセリングヘッドホン', '外音を遮断する高性能ANC搭載。',
        'audio', 'ヘッドホン,ANC,無線', 32800, TRUE);"

sleep 3
curl -s "localhost:9200/products/_count?pretty"
```

件数が1つ増えます。

### 削除（DELETE）

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb \
  -e "DELETE FROM products WHERE name = 'ノイズキャンセリングヘッドホン';"

sleep 3
curl -s "localhost:9200/products/_count?pretty"
```

**件数が元に戻れば成功です。** 第13章で「ポーリング方式では絶対に解決できない」と書いた削除の伝播が、tombstone（今回は `__deleted` フラグ）によって実現されました。

```
MySQL: DELETE            Kafka: __deleted=true          ES: DELETE /products/_doc/8
   ─────────────────────────────────────────────────────────────────────►
                          第13章の論点2、ここに完結
```

### 遅延を測ってみる

```bash
docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --describe --group logstash-es-sink
```

`LAG` が 0 なら、Kafka に届いたものはすべて ES に反映済みという意味です。第14章で学んだこのコマンドが、そのまま**パイプラインの健康診断**になります。

---

## 16.6 参考 ― Kafka Connect の Elasticsearch シンクを使う場合

実務でよく採用されるのはこちらです。Logstash 経路と設計上の論点は同じですが、設定の書き方が変わります。

### プラグインの導入

Debezium の Connect イメージには ES シンクが入っていないため、追加が必要です。

```dockerfile
FROM quay.io/debezium/connect:3.5
# ダウンロードしたコネクタのzipを展開して配置する
COPY ./plugins/elasticsearch-connector /kafka/connect/elasticsearch-connector
```

導入できたかは、ワーカーに聞くのが確実です。**コネクタのクラス名は配布元によって異なる**ので、必ずここで確認してください。

```bash
curl -s localhost:8083/connector-plugins | jq -r '.[].class' | grep -i elastic
```

### 設定のポイント（`register-es-sink.json`）

```json
{
  "name": "es-sink",
  "config": {
    "connector.class": "（上のコマンドで確認したクラス名）",
    "topics": "shop.shopdb.products",
    "connection.url": "http://elasticsearch:9200",

    "key.ignore": "false",
    "schema.ignore": "true",
    "write.method": "upsert",
    "behavior.on.null.values": "delete",

    "errors.tolerance": "all",
    "errors.deadletterqueue.topic.name": "dlq.es-sink",
    "errors.deadletterqueue.topic.replication.factor": "1",

    "transforms": "extractKey,routeIndex",
    "transforms.extractKey.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
    "transforms.extractKey.field": "id",
    "transforms.routeIndex.type": "org.apache.kafka.connect.transforms.RegexRouter",
    "transforms.routeIndex.regex": "shop\\.shopdb\\.products",
    "transforms.routeIndex.replacement": "products"
  }
}
```

| 設定 | 意味 | なぜ重要か |
|---|---|---|
| `key.ignore: false` | Kafkaのキーを `_id` に使う | **これが冪等性の要**。`true` にすると `topic+partition+offset` がIDになり、再送で重複する |
| `ExtractField$Key` | キーの `{"id":1}` から `1` を取り出す | そのままだと `_id` が `Struct{id=1}` のような文字列になる |
| `behavior.on.null.values: delete` | tombstoneを削除に変換 | **既定では無視される**。削除が反映されない事故の定番 |
| `RegexRouter` | トピック名 → インデックス名の変換 | 既定ではトピック名がそのままインデックス名になってしまう |
| `write.method: upsert` | 部分更新として書く | `insert` だと既存ドキュメントと衝突しうる |

※ この経路では、SMT側は `delete.tombstone.handling.mode` を **`tombstone`** にします（値が `null` のメッセージを届ける必要があるため）。Logstash 経路の `rewrite` とは逆です。

### DLQ（Dead Letter Queue）

シンクコネクタの強みがこれです。

```
        正常なメッセージ ──────────────────► Elasticsearch
              │
     変換や書き込みに失敗
              │
              └──────────────────────────► dlq.es-sink トピック
                                             （原因はヘッダに記録される）
```

`errors.tolerance: all` にすると、1件の不正データでコネクタ全体が停止せず、**問題のメッセージだけをDLQトピックに退避**して処理を続けます。原因を調べるにはDLQを読みます。

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic dlq.es-sink \
  --from-beginning --property print.headers=true
```

> ⚠️ **DLQはシンクコネクタ専用の機能です。** Debezium のようなソースコネクタでは使えません（読み取り元のDBは「捨てて先へ進む」ということができないため）。ソース側の失敗は、コネクタの停止として現れます。
> また `errors.tolerance: all` は諸刃の剣です。**気づかないうちにデータが欠けていく**ので、DLQの件数を必ず監視してください（第19章）。

---

## まとめ

- Kafka → ES の手段は「Kafka Connect ESシンク」「Logstash」「自作コンシューマ」の3つ。**設計上の論点はどれを選んでも同じ**
- CDCを流し込む前に、**マッピングを明示的に作る**。未知フィールドの扱いは `dynamic` で決める（`true` は事故のもと、まずは `false`）
- **SMT `ExtractNewRecordState`（unwrap）** で、Debeziumの入れ子イベントを `after` の中身だけに平坦化する。`add.fields` でメタデータを `__` 付きで残せる
- **`delete.tombstone.handling.mode` は必ず明示する。** 既定の `drop` では削除が下流に届かない。Logstash なら `rewrite`、Connect ESシンクなら `tombstone`
- **`_id` にMySQLの主キーを使うこと**が、at-least-once 配信に対する冪等性の担保になる（Logstashなら `document_id`、Connectなら `key.ignore=false`）
- Logstash では `manage_template => false` を忘れない。忘れると自作マッピングが上書きされる
- MySQL と ES の型差（`TINYINT(1)`→`boolean`、カンマ区切り→配列、`DATETIME`→エポックミリ秒）は、パイプライン側で吸収する
- シンクコネクタには **DLQ** があり、不正データで全体を止めずに退避できる。ただしDLQの監視が前提

---

## 理解度チェック

**問1.** Logstash の設定で `document_id => "%{[id]}"` を指定しなかった場合、何が起きますか。第13章の「at-least-once」と関連づけて説明してください。

**問2.** `delete.tombstone.handling.mode` を既定のまま（指定なし）にすると、MySQLで `DELETE` を実行したときElasticsearchはどうなりますか。

**問3.** Logstash 経路では `rewrite`、Kafka Connect ESシンク経路では `tombstone` を選びました。なぜ下流によって選択が変わるのですか。

**問4.** Logstash の output で `manage_template => false` を指定し忘れると、どんな不都合がありますか。

**問5.** ESシンクコネクタで `errors.tolerance: all` と DLQ を設定しました。この設定の利点と、必ずセットで必要になる運用上の対策を述べてください。

<details>
<summary>解答を見る</summary>

**問1.**
`_id` を指定しないと Elasticsearch が自動採番するため、**同じ商品のイベントが届くたびに別のドキュメントが作られる**。
Kafka の配信保証は at-least-once であり、コネクタやLogstashの再起動時に同じイベントが再送されうる。また、同じ商品が更新されるたびに新しいイベントが流れる。結果として検索結果に同じ商品が何件も重複して現れ、削除も特定のドキュメントに効かなくなる。
主キーを `_id` にすれば「同じIDなら上書き」となり、**何回適用しても結果が同じ（冪等）** になる。

**問2.**
既定は `drop` なので、**削除イベントも tombstone も下流に届かず、Elasticsearch にドキュメントが残り続ける**。MySQL上は存在しない商品が検索結果に出続ける、という第13章で挙げた最悪の症状になる。

**問3.**
下流が「削除」をどう受け取れるかが違うため。
- Kafka Connect ESシンクは、**値が `null` のメッセージ（tombstone）を受け取ると削除する**という仕組みを持っているので `tombstone` が適する
- Logstash の Kafka入力は値が `null` のメッセージを素直に扱えないため、削除を**通常のメッセージ＋`__deleted: "true"` という目印**として受け取れる `rewrite` が適する。Logstash側では、このフラグを見て `action => "delete"` に振り分ける

**問4.**
Logstash が既定のインデックステンプレートを登録し、**自分で定義した kuromoji／シノニムのアナライザー設定やマッピングが上書き・無視される**恐れがある。フェーズ2で作り込んだ日本語検索の品質が失われ、`text` フィールドが標準アナライザーで解析されるなどの不整合が起きる。

**問5.**
利点：不正なメッセージが1件混ざってもコネクタ全体が停止せず、**問題のあるメッセージだけをDLQトピックへ退避して処理を継続できる**。原因はDLQのヘッダに記録されるため、後から調査できる。
必要な対策：**DLQの件数を監視すること**。`errors.tolerance: all` は「失敗しても止まらない」設定であり、放置すると気づかないうちにElasticsearch側のデータが欠落していく。DLQに1件でも入ったらアラートを上げ、内容を確認して再投入する運用が前提になる。

</details>

---

## 次の章へ

パイプラインは通りました。ここからは「**壊れないようにする**」フェーズです。

次章では、CDCを運用に載せると必ず起きる3つの出来事に備えます。**上流で `ALTER TABLE` が実行されたとき**、**古いイベントが後から届いたとき**、そして**マッピングを変えたくなってインデックスを作り直したいとき**。とくに最後の「無停止でのインデックス再構築」は、検索システムを運用するなら必ず身につけたい技術です。

> **次章：第17章「スキーマ変更・順序・再同期 ― 壊さずに作り直す技術」**

---

### この章のキーワード

シンク（sink）/ Logstash / パイプライン（input / filter / output）/ `[@metadata]` / `document_id` / `manage_template` / SMT（Single Message Transform）/ `ExtractNewRecordState`（unwrap）/ `add.fields` / `delete.tombstone.handling.mode`（drop / tombstone / rewrite）/ `__deleted` / 冪等性 / `_id` に主キー / `key.ignore` / `behavior.on.null.values` / `RegexRouter` / `ExtractField$Key` / `write.method` / DLQ（Dead Letter Queue）/ `errors.tolerance` / `dynamic`（true / false / strict）/ コンシューマラグ
