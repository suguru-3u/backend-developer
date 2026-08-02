# 第17章　スキーマ変更・順序・再同期 ― 壊さずに作り直す技術

> Elasticsearch学習教科書 ― フェーズ3「CDCパイプライン構築」
> 前提知識：第13〜16章（動作するCDCパイプラインがあること）

パイプラインは通りました。しかし「動いた」と「運用できる」の間には、まだ距離があります。

CDCを本番に置くと、遅かれ早かれ次の3つが必ず起こります。

1. 上流で **`ALTER TABLE`** が実行される
2. **古いイベントが、新しいイベントより後に**適用されそうになる
3. マッピングを変えたくなり、**インデックスを作り直したくなる**

この章では、この3つに備えます。特に3つ目の「無停止でインデックスを作り直す技術」は、検索システムを運用するなら必ず身につけておきたいものです。

---

## この章で学ぶこと（学習目標）

この章を読み終えると、次のことができるようになります。

- 冪等性だけでは防げない「順序」の問題を説明できる
- **外部バージョニング（`version_type=external`）** で古いイベントによる上書きを防げる
- `ALTER TABLE` が CDC パイプラインをどう通過するかを追える
- ESのマッピングで**できる変更／できない変更**を区別できる
- **エイリアス＋再インデックス**で、検索を止めずにインデックスを作り直せる
- 再同期（re-sync）の3つのレベルを使い分けられる。**増分スナップショット**を実行できる

---

## 17.1 冪等なのに壊れる ― 順序の問題

第16章で `_id` に主キーを使い、「同じイベントが何回来ても結果は同じ」にしました。しかし、これで守れるのは**同じイベントの重複**だけです。**違うイベントが逆順に届く**ケースは守れません。

```
Kafkaトピック（正しい順序で並んでいる）
   offset 25 : price = 89000
   offset 26 : price = 79000     ← 最新

                    ↓ 何らかの理由で 26 の適用が失敗し、リトライされる

   時刻 t1 : offset 26 を適用しようとして失敗（ESが一時的に応答なし）
   時刻 t2 : offset 25 が別のワーカーから適用される  → ES: 89000
   時刻 t3 : offset 26 のリトライが成功              → ES: 79000  ✓ セーフ

   ─────────────────────────────────────────────────

   別のシナリオ：オフセットを巻き戻して再処理した
   時刻 t1 : offset 25 を再適用                      → ES: 89000
             （その間に新しい更新 79000 は既に反映済みだった）
                                                     → ES: 89000  ✗ 巻き戻った
```

とくに危険なのが、**第14章で学んだオフセットのリセット**と、**シンクの並列化（`tasks.max` を増やす／Logstashの `consumer_threads` を増やす）** です。「再処理したら価格が古い値に戻った」という事故は、CDCの定番トラブルです。

### 解決策：Elasticsearch に「時計」を渡す

Elasticsearch には **外部バージョニング** という機能があります。書き込みのたびにバージョン番号を渡し、

> **今ESに入っている版より新しくなければ、書き込みを拒否する**

という動作をさせられます。

```
ES内のドキュメント: _version = 1785312000100

  ① 新しいイベント (version = 1785312060000) → 1785312000100 < 1785312060000 → 適用 ✓
  ② 古いイベント   (version = 1785311900000) → 1785311900000 < 1785312000100 → 拒否 ✗
                                                （409 version_conflict）
```

**何をバージョン番号にするか** ―― 第13章で予告した MySQL 固有の設計課題がここです。

| 候補 | 長所 | 短所 |
|---|---|---|
| `source.ts_ms`（コミット時刻） | 単純。単調増加する | **同一ミリ秒内の連続更新**を区別できない |
| `file` + `pos`（binlog座標） | 完全に正確な順序 | 2値なので合成が必要。ファイルがローテートすると連続性の扱いが煩雑 |
| `updated_at` 列 | わかりやすい | アプリが更新し忘れると機能しない |

現実解は **`source.ts_ms` を使い、`external_gte`（同値も許可）にする**ことです。同一ミリ秒内の連続更新は「どちらが後でも大差ない」と割り切る判断です。厳密さが要る場合は `file` と `pos` を合成した数値を作ります。

### Logstash に適用する

第16章の `filter` から `__source_ts_ms` を消していましたが、これを**残して使います**。

```
filter {
  # ...（第16章と同じ）...

  # バージョン番号として使うので、__source_ts_ms は消さずに退避する
  mutate { add_field => { "[@metadata][version]" => "%{[__source_ts_ms]}" } }

  mutate {
    remove_field => ["@version", "event",
                     "__deleted", "__op",
                     "__source_ts_ms", "__source_file", "__source_pos"]
  }
}

output {
  elasticsearch {
    hosts           => ["http://elasticsearch:9200"]
    index           => "products"
    document_id     => "%{[id]}"
    action          => "%{[@metadata][action]}"
    version         => "%{[@metadata][version]}"
    version_type    => "external_gte"
    manage_template => false

    # 古いイベントが弾かれるのは"正常動作"なので、ログを静かにする
    silence_errors_in_log => ["version_conflict_engine_exception"]
  }
}
```

> ⚠️ **`silence_errors_in_log` はLogstashのバージョンで名前が違うことがあります**（古い版では `failure_type_logging_whitelist`）。起動時に「unknown setting」と言われたら、もう一方の名前を試してください。

> ⚠️ **削除には落とし穴があります**
> 外部バージョニングで削除したドキュメントの「墓標」は、Elasticsearch 内に `index.gc_deletes`（既定60秒）の間だけ保持されます。この時間を過ぎてから**削除より古いイベント**が届くと、比較対象がないため**削除したはずのドキュメントが復活します**。
> 大量のリプレイを行うときは、この時間を一時的に延ばす（`"index.gc_deletes": "1h"` など）か、後述の「インデックスを作り直す」方式を選ぶのが安全です。

---

## 17.2 `ALTER TABLE` がパイプラインを通るとき

上流で列が追加されたら、何が起きるでしょうか。実際にやってみます。

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb -e "
ALTER TABLE products ADD COLUMN release_date DATE NULL;
UPDATE products SET release_date = '2026-04-01' WHERE id = 1;"
```

### ① Debezium は自動で追従する

DDLは binlog に載ります。Debezium はそれを読み、**スキーマ履歴トピックに新しいテーブル定義を記録**して、以降のイベントに新しい列を含めます。第15章で「履歴トピックが必要な理由」として説明したことが、ここで効いています。**コネクタの再登録は不要です。**

```bash
docker compose exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic shop.shopdb.products \
  --property print.key=true | tail -n 1
```

```json
{ "id":1, "name":"...", "price":79000, "release_date": 20544, ... }
```

### ② ここで気づくべき異変 ― `20544` とは何か

日付なのに数値です。**Debezium は MySQL の `DATE` 型を「1970-01-01からの日数」に変換します**（`io.debezium.time.Date`）。

これをそのまま ES の `date` 型フィールドに入れると、ESは**エポックミリ秒**として解釈するため、`20544ミリ秒` ＝ **1970年1月1日 00:00:20** になってしまいます。誰も気づかないまま、全商品の発売日が1970年になる、という典型的な事故です。

Logstash で変換します。

```
  # DATE型（エポックからの日数）→ ISO日付文字列
  if [release_date] {
    ruby {
      code => 'event.set("release_date",
                 (Time.at(0).utc + event.get("release_date").to_i * 86400)
                   .strftime("%Y-%m-%d"))'
    }
  }
```

| MySQLの型 | Debeziumの表現 | 対処 |
|---|---|---|
| `DATE` | エポックからの**日数**（int32） | 上記のように変換 |
| `DATETIME` | エポック**ミリ秒**（int64） | ESの `date` でそのまま扱える |
| `TIMESTAMP` | ISO8601の文字列（UTC） | そのままでよい |
| `DECIMAL` | **Base64文字列**（既定）| `decimal.handling.mode: double` または `string` を検討 |
| `BOOLEAN` | `1` / `0` | 第16章のとおり変換 |

> 💡 `DECIMAL` の既定挙動は特に驚きます。精度を失わないためにバイト列をBase64で送るのですが、価格や金額をそのまま入れると意味不明な文字列がESに入ります。金額を扱うテーブルでは、コネクタ設定に `"decimal.handling.mode": "string"` を足すのが安全です。

### ③ Elasticsearch 側はどうなるか

`dynamic: false` にしてあるので、**`release_date` は `_source` に入るが検索・集計の対象にならない**という状態です。エラーは出ません。静かに無視されます。

これは良し悪しがあります。

| 設定 | 新しい列が来たとき | 評価 |
|---|---|---|
| `dynamic: true` | 型を推測して自動追加 | 便利だが、`20544` が `long` になるなど**間違った型で固定**されるリスク |
| `dynamic: false` | 無視（`_source`には残る） | 安全だが、**気づかない** |
| `dynamic: strict` | **エラーで拒否** | 気づける。ただしパイプラインが止まる |

**CDCでの推奨は `strict` です。** 「静かに無視される」より「止まって気づく」ほうが、データの正しさを守れるからです。ただし止まると困る場合は `false` にして、代わりに「未知フィールドの監視」を仕込みます。判断の分かれ目は、**そのインデックスが検索の正本かどうか**です。

### ④ 新しい列を検索対象にする

**フィールドの「追加」は既存インデックスにできます。**

```bash
curl -X PUT "localhost:9200/products/_mapping" -H 'Content-Type: application/json' -d '
{ "properties": { "release_date": { "type": "date" } } }'
```

ただし、**すでに入っているドキュメントは自動では再処理されません**。`_source` には値があるので、次の方法で反映します。

```bash
# _source の値を使って、既存ドキュメントを再インデックスする
curl -X POST "localhost:9200/products/_update_by_query?conflicts=proceed&refresh"
```

（`_update_by_query` はボディなしでも「全ドキュメントを読み直して書き戻す」動作をします。）

---

## 17.3 できる変更・できない変更

第3章で学んだ「型は後から変えられない」が、運用ではこう効いてきます。

| やりたいこと | できるか | 方法 |
|---|---|---|
| 新しいフィールドを追加 | ✅ できる | `PUT /_mapping` |
| `text` に `keyword` サブフィールドを追加 | ✅ できる | `PUT /_mapping` |
| `integer` → `long` などの型変更 | ❌ **できない** | 再インデックス |
| アナライザーの変更 | ❌ **できない**（既存フィールド） | 再インデックス |
| シノニム辞書の更新 | △ 条件付き | インデックスを閉じて更新、または再インデックス |
| フィールドの削除 | ❌ マッピングからは消せない | 再インデックス |
| シャード数の変更 | ❌ できない | 再インデックス（または `_split` / `_shrink`） |

つまり、**運用を続けていれば「作り直し」は必ず発生します。** そのときに検索を止めないための技術が、次節のエイリアスです。

---

## 17.4 エイリアスによる無停止インデックス再構築

### 考え方

アプリケーションには**インデックスの実名を絶対に使わせず、エイリアス（別名）だけを見せます**。

```
  【アプリから見える名前】        【実体】

        products  ─────────────►  products_v1
       （エイリアス）

                   ↓ 切り替えは一瞬（アトミック）

        products  ─────────────►  products_v2
```

これができると、
1. 新しいマッピングで `products_v2` を作る
2. データを流し込む（時間がかかってよい）
3. **エイリアスを一瞬で切り替える**
4. 問題があれば、エイリアスを戻すだけでロールバック

という運用ができます。アプリは何も知らないまま、検索が止まりません。

> 📌 **教訓：最初からエイリアスにしておく。**
> 私たちは第16章で `products` という**実インデックス**を作ってしまいました。これは学習の都合ですが、実務では**最初から `products_v1` を作り、`products` はエイリアスにする**のが定石です。以下でその形に移行します。

### 移行する

```bash
# 1. 新しいマッピングでv2を作る（release_date を最初から date 型で定義）
curl -X PUT "localhost:9200/products_v2" -H 'Content-Type: application/json' \
  --data @phase3-16/products-index.json

curl -X PUT "localhost:9200/products_v2/_mapping" -H 'Content-Type: application/json' -d '
{ "properties": { "release_date": { "type": "date" } } }'

# 2. 既存データを移す
curl -X POST "localhost:9200/_reindex?wait_for_completion=true" \
  -H 'Content-Type: application/json' -d '
{
  "source": { "index": "products" },
  "dest":   { "index": "products_v2" }
}'

# 3. 件数が一致することを確認（ここ重要）
curl -s "localhost:9200/products/_count?pretty"
curl -s "localhost:9200/products_v2/_count?pretty"

# 4. 旧インデックスを消し、同じ名前をエイリアスとして v2 に向ける
curl -X DELETE "localhost:9200/products"

curl -X POST "localhost:9200/_aliases" -H 'Content-Type: application/json' -d '
{ "actions": [ { "add": { "index": "products_v2", "alias": "products" } } ] }'
```

以降は `products` という名前で検索も書き込みもできますが、実体は `products_v2` です。

### 次回からの切り替えはアトミックに

一度エイリアス運用にしてしまえば、次の作り直しは**一瞬・無停止**です。

```bash
curl -X POST "localhost:9200/_aliases" -H 'Content-Type: application/json' -d '
{
  "actions": [
    { "remove": { "index": "products_v2", "alias": "products" } },
    { "add":    { "index": "products_v3", "alias": "products" } }
  ]
}'
```

**この2つの操作は1つのリクエストの中で同時に適用されます。** 「どちらも指していない一瞬」が発生しません。これがエイリアスの核心です。

> ⚠️ **書き込み先としてエイリアスを使う場合の注意**
> エイリアスが複数のインデックスを指していると、書き込み時に「どれに書けばいいか」が決まらずエラーになります。複数を束ねる場合は `"is_write_index": true` で書き込み先を1つ明示してください。今回のように1対1なら不要です。

---

## 17.5 再同期（re-sync）の3つのレベル

「ESのデータが壊れた／作り直したい」というとき、**どこまで巻き戻すか**で手段が変わります。軽い順に見ていきます。

### レベル1：Kafkaのオフセットを巻き戻す（数分）

Kafka に残っている範囲を、シンクだけ再適用します。**上流には一切触りません。**

```bash
# Logstashを止める
docker compose stop logstash

# コンシューマグループを先頭に戻す
docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group logstash-es-sink --topic shop.shopdb.products \
  --reset-offsets --to-earliest --execute

docker compose start logstash
```

**使える条件**：必要なイベントが Kafka の保持期間内に残っていること。第14章の「リプレイ」そのものです。

### レベル2：増分スナップショット（数分〜数時間）

Kafka にもう残っていない、あるいは「そもそもDBの現在値が正しいか自信がない」というとき、**Debeziumを止めずに、テーブルを読み直させる**ことができます。これが **増分スナップショット（incremental snapshot）** です。

まず、Debeziumに指示を出すための**シグナルテーブル**を用意します。

```sql
CREATE TABLE debezium_signal (
  id   VARCHAR(42)   PRIMARY KEY,
  type VARCHAR(32)   NOT NULL,
  data VARCHAR(2048) NULL
);
```

コネクタ設定に2行追加します（シグナルテーブル自身もCDC対象に含める必要があります）。

```json
  "table.include.list": "shopdb.products,shopdb.debezium_signal",
  "signal.data.collection": "shopdb.debezium_signal"
```

あとは、**このテーブルに1行INSERTするだけ**で再読み込みが始まります。

```sql
INSERT INTO debezium_signal (id, type, data) VALUES
 ('resync-2026-08-03', 'execute-snapshot',
  '{"data-collections": ["shopdb.products"], "type": "incremental"}');
```

Debezium はテーブルを小さなチャンクに分割し、**通常の binlog 追従を続けながら**少しずつ読み出して Kafka に流します。**サービスを止める必要も、コネクタを作り直す必要もありません。** CDC運用で最も頼りになる機能です。

### レベル3：全部やり直す（数時間）

スキーマ履歴を壊した、あるいは binlog がパージされて追従不能になった場合の最終手段です。

```
 1. 新しいインデックス products_v3 を新マッピングで作る
 2. コネクタを削除し、topic.prefix と履歴トピック名を変えて再登録
      → 初期スナップショットが最初から走る
 3. Logstash の group_id も変えて、新トピックを頭から読ませる
 4. 件数と内容を検証する
 5. エイリアス products を v3 に切り替える（アトミック）
 6. 古いコネクタ・トピック・インデックスを片付ける
```

ポイントは、**新旧を並走させてから切り替える**ことです。エイリアスを使っていれば、利用者に影響を与えずにこれができます。

| レベル | 巻き戻す範囲 | 所要 | 上流への影響 |
|---|---|---|---|
| 1 | Kafkaのオフセット | 数分 | なし |
| 2 | DBのテーブルを読み直し | 数分〜数時間 | 軽い読み取り負荷 |
| 3 | すべて作り直し | 数時間 | スナップショットの負荷 |

---

## まとめ

- `_id` に主キーを使う冪等性は「同じイベントの重複」を防ぐが、**「古いイベントが後から適用される」ことは防げない**
- **外部バージョニング（`version_type=external_gte`）** に `source.ts_ms` を渡すことで、古い版による上書きをESが拒否するようになる。バージョン衝突（409）は**正常動作**なのでログを静かにしてよい
- 外部バージョニング下では、削除の墓標が `index.gc_deletes`（既定60秒）で消えるため、長時間のリプレイでは**削除が復活しうる**
- `ALTER TABLE` は Debezium が**自動で追従**する（スキーマ履歴トピックのおかげ）。コネクタの再登録は不要
- **MySQLの型はそのままESに入らない。** `DATE` はエポックからの**日数**、`DECIMAL` は既定でBase64文字列。無変換だと静かに壊れる
- ESのマッピングは**追加はできるが、型やアナライザーの変更はできない**。運用を続ければ作り直しは必ず発生する
- **エイリアス運用が前提。** アプリには別名だけを見せ、`_aliases` の remove+add を1リクエストで行えば無停止・アトミックに切り替えられる
- 再同期は3レベル。①Kafkaオフセットの巻き戻し ②**増分スナップショット**（シグナルテーブルに1行INSERT） ③全部作り直し（新旧並走→エイリアス切替）

---

## 理解度チェック

**問1.** `_id` に主キーを使っているのに、オフセットを巻き戻して再処理したら商品の価格が古い値に戻ってしまいました。なぜですか。どう防ぎますか。

**問2.** 外部バージョニングのバージョン番号に `source.ts_ms` を使う場合の弱点と、`external` ではなく `external_gte` を選ぶ理由を述べてください。

**問3.** MySQL で `ALTER TABLE products ADD COLUMN release_date DATE;` を実行しました。(a) Debezium側で必要な作業、(b) Elasticsearch側で必要な作業を、それぞれ答えてください。

**問4.** `DATE` 型の列を無変換でESの `date` フィールドに入れると、何が起きますか。

**問5.** マッピングのアナライザーを変更したくなりました。検索を止めずに切り替える手順を述べてください。

<details>
<summary>解答を見る</summary>

**問1.**
冪等性（`_id` に主キー）が保証するのは「同じイベントを何度適用しても結果が同じ」ということだけで、**適用の順序は保証しない**。オフセットを巻き戻すと、すでに反映済みの新しい値の上に、古いイベントが後から上書きされてしまう。
防ぐには**外部バージョニング**を使い、`source.ts_ms` などの単調増加する値をバージョン番号としてESに渡す。ESは現在の版より古い書き込みを 409（version conflict）で拒否するようになる。

**問2.**
弱点：**同一ミリ秒内に同じ行が複数回更新された場合、順序を区別できない**。
`external_gte` を選ぶ理由：`external` は「厳密により大きいバージョン」しか受け付けないため、同一ミリ秒の更新が拒否されて**最後の更新が反映されない**恐れがある。`external_gte` は同値も受け付けるので、この取りこぼしを避けられる。厳密な順序が必要なら `file` と `pos` を合成した値を使う。

**問3.**
(a) **不要**。DDLは binlog に載り、Debezium がスキーマ履歴トピックに記録して自動的に追従する。以降のイベントには新しい列が含まれる。
(b) 2つ必要。
1. `PUT /products/_mapping` で `release_date` フィールドを追加する（フィールドの追加は可能）
2. すでに入っているドキュメントには反映されないため、`_update_by_query` で再インデックスするか、上流で該当行を再度読み込ませる
加えて、`DATE` 型の値変換をパイプライン側に実装する必要がある（問4参照）。

**問4.**
Debezium は MySQL の `DATE` を「1970-01-01 からの**日数**」（例：`20544`）として送るが、Elasticsearch の `date` 型は数値を**エポックミリ秒**として解釈する。結果、`20544` は 1970年1月1日 00:00:20 と記録され、**全商品の日付が1970年になる**。しかもエラーは出ないため気づきにくい。パイプライン側で日数→日付文字列に変換する必要がある。

**問5.**
アナライザーは既存フィールドに対して変更できないため、インデックスの作り直しが必要。エイリアスを使えば無停止でできる。
1. 新しいアナライザー設定で `products_v3` を作成する
2. `_reindex` で既存データを移す（またはKafkaのオフセットを巻き戻してパイプラインから流し込む）
3. 件数と検索結果を検証する
4. `POST /_aliases` で `remove: v2` と `add: v3` を**1リクエストにまとめて**実行し、アトミックに切り替える
5. 問題があればエイリアスを v2 に戻すだけでロールバックできる。安定したら旧インデックスを削除する

</details>

---

## 次の章へ

ここまでの `products` テーブルは、意図的に「1テーブルで完結する」形にしてありました。`tags` をカンマ区切りの文字列にしてあったのも、そのためです。

しかし現実のDBは正規化されています。`categories` テーブル、`product_tags` 中間テーブル、`reviews` テーブル……。一方 Elasticsearch は「1ドキュメントに検索したい情報が全部入っている」ことを好みます。

次章は、CDC × 検索インデックスの**本丸**です。**テーブル単位で流れてくるイベントを、どうやって1つのドキュメントに合流させるのか。** そして「カテゴリ名を1つ変えたら、何千件の商品ドキュメントを更新しなければならない」という問題にどう立ち向かうのか。実務でいちばん設計が問われるところです。

> **次章：第18章「非正規化とストリームJOIN ― 複数テーブルを1ドキュメントに」**

---

### この章のキーワード

順序の逆転 / 外部バージョニング / `version_type`（`external` / `external_gte`）/ `version_conflict_engine_exception` / `index.gc_deletes` / `ALTER TABLE` / スキーマ履歴トピック / `io.debezium.time.Date` / `decimal.handling.mode` / `dynamic`（true / false / strict）/ `PUT /_mapping` / `_update_by_query` / `_reindex` / エイリアス（alias）/ `_aliases` のアトミック切替 / `is_write_index` / 再同期（re-sync）/ オフセットリセット / **増分スナップショット** / シグナルテーブル / `signal.data.collection` / `execute-snapshot`
