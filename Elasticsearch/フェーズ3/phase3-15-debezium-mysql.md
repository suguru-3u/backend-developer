# 第15章　Debezium で MySQL の変更を捕捉する ― Kafka Connect と初期スナップショット

> Elasticsearch学習教科書 ― フェーズ3「CDCパイプライン構築」
> 前提知識：第13章（CDCの設計）／第14章（Kafka基礎）
> 付属ファイル：`phase3-15/docker-compose.yml`、`phase3-15/mysql-init/01-init.sql`、`phase3-15/register-mysql-source.json`

前章で Kafka という土管が通りました。この章では、その上流に **MySQL** と **Debezium** をつなぎます。

この章のゴールはただ一つ、**`UPDATE products SET price = 89000 WHERE id = 1` を実行した瞬間に、Kafka のコンシューマ画面へ変更イベントが流れてくるのを自分の目で見ること**です。CDCがいちばん気持ちよく見える瞬間です。

---

## この章で学ぶこと（学習目標）

この章を読み終えると、次のことができるようになります。

- CDC のために MySQL 側に必要な設定（binlog、`server-id`、権限）を説明・適用できる
- **Kafka Connect** の役割と、ワーカー／コネクタ／タスクの関係を説明できる
- REST API で Debezium MySQL コネクタを登録し、状態を確認できる
- **初期スナップショット**（`op: "r"`）と、その後の binlog 追従の切り替わりを理解する
- 生成された変更イベントJSONを読み解ける
- **スキーマ履歴トピック**がなぜ必要で、なぜ圧縮してはいけないのかを説明できる
- 典型的な失敗（権限不足、`server-id` 重複、公開鍵取得エラー）を切り分けられる

---

## 15.1 Kafka Connect とは ― ワーカー・コネクタ・タスク

まず用語を整理します。ここが曖昧なままだと、エラーメッセージが読めません。

```
 ┌──────────────────────────────────────────────────┐
 │  Kafka Connect ワーカー（= 1つのJVMプロセス）        │
 │  REST API: http://localhost:8083                 │
 │                                                  │
 │   ┌────────────────────┐  ┌────────────────────┐ │
 │   │ コネクタ            │  │ コネクタ            │ │
 │   │ "products-source"  │  │ "es-sink"          │ │
 │   │  ＝ 設定と管理の単位  │  │                    │ │
 │   │  ┌──────────────┐  │  │  ┌──────────────┐  │ │
 │   │  │ タスク #0     │  │  │  │ タスク #0     │  │ │
 │   │  │ ＝実際に働く人 │  │  │  ├──────────────┤  │ │
 │   │  └──────────────┘  │  │  │ タスク #1     │  │ │
 │   └────────────────────┘  │  └──────────────┘  │ │
 │                            └────────────────────┘ │
 └──────────────────────────────────────────────────┘
```

| 用語 | 意味 |
|---|---|
| **ワーカー** | コネクタを動かすプロセス。今回は `connect` コンテナ1つ |
| **コネクタ** | 「何をどこへ運ぶか」という**設定の単位**。JSONで定義しREST APIに登録する |
| **タスク** | コネクタが生成する**実行の単位**。`tasks.max` で並列度を指定する |
| **プラグイン** | コネクタの実体（jar）。ワーカーの `plugin.path` に置く |

重要な性質が3つあります。

1. **設定はKafkaに保存される**（`connect_configs` トピック）。ワーカーを再起動しても、登録済みのコネクタは自動で復活します
2. **進捗もKafkaに保存される**（`connect_offsets` トピック）。「binlogのどこまで読んだか」がここに記録されるため、ワーカーが落ちても続きから再開できます
3. **Debezium MySQLコネクタのタスクは常に1つ**です。binlogは1本のストリームなので、分割して並列に読むことができません（`tasks.max` に何を書いても1になります）

> 📌 第14章で「コンシューマがどこまで読んだかは `__consumer_offsets` に入る」と学びました。Connect の**ソース**コネクタは Kafka から読むわけではないので、代わりに `connect_offsets` に「ソース側のどこまで読んだか（binlogのファイル名と位置）」を記録します。役割は同じで、保存先が違うだけです。

---

## 15.2 MySQL 側の準備

### 必要な設定

付属の `docker-compose.yml` では、MySQL をこう起動しています。

```yaml
  mysql:
    image: mysql:8.4
    command:
      - --server-id=1
      - --log-bin=binlog
      - --binlog-row-image=FULL
      - --gtid-mode=ON
      - --enforce-gtid-consistency=ON
      - --binlog-expire-logs-seconds=604800
```

| 設定 | 意味 | 備考 |
|---|---|---|
| `server-id` | レプリケーション上の一意な番号 | **必須**。Debezium 側にも別の値を割り当てる |
| `log-bin` | binlog を有効化 | MySQL 8.0 以降は**既定でON**。明示しておくと安心 |
| `binlog-format=ROW` | 行の値を記録する形式 | MySQL 8.0.34 以降は**非推奨設定**で、既定が `ROW`。**あえて書かない**のが正解 |
| `binlog-row-image=FULL` | 変更前後の**全列**を記録 | `MINIMAL` だと `before` が欠け、削除やスキーマ変更でつまずく |
| `gtid-mode=ON` | グローバルトランザクションID | 必須ではないが、位置管理が堅牢になるため推奨 |
| `binlog-expire-logs-seconds` | binlog の保持期間 | 第13章で触れた「パージされると全件やり直し」の設定 |

> ⚠️ **`binlog_format` は書かないでください**
> ネット上の記事の多くには `--binlog-format=ROW` がありますが、MySQL 8.0.34 でこの変数は非推奨となり、既定値が `ROW` になりました。MySQL 9系では削除されています。書くと起動時に警告が出るだけで害はありませんが、**もはや不要**です。設定されているかは次で確認できます。
> ```sql
> SHOW VARIABLES LIKE 'binlog_format';   -- ROW であればOK
> SHOW VARIABLES LIKE 'log_bin';         -- ON であればOK
> ```

### CDC用ユーザーの権限

`mysql-init/01-init.sql` で、次のユーザーを作っています。

```sql
CREATE USER 'debezium'@'%' IDENTIFIED BY 'dbzpw';
GRANT SELECT, RELOAD, SHOW DATABASES,
      REPLICATION SLAVE, REPLICATION CLIENT, LOCK TABLES
  ON *.* TO 'debezium'@'%';
```

| 権限 | 何に使うか |
|---|---|
| `SELECT` | **初期スナップショット**で既存の全行を読む |
| `RELOAD` / `LOCK TABLES` | スナップショット開始時に一貫した断面を取る |
| `SHOW DATABASES` | 対象データベースの列挙 |
| `REPLICATION SLAVE` | **binlog をレプリカとして受け取る**（本体） |
| `REPLICATION CLIENT` | binlog の現在位置（`SHOW MASTER STATUS` 等）の問い合わせ |

第13章で「Debezium は1台のレプリカとして振る舞う」と書きました。`REPLICATION SLAVE` 権限が必要なのは、まさにそのためです。

### 起動と確認

```bash
docker compose up -d --build
docker compose ps
```

MySQLに入ってデータを確認します。

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb -e "SELECT id, name, price, in_stock FROM products;"
```

6件の商品が入っていれば準備完了です。フェーズ1で手で `bulk` 投入していたあのデータが、今度は**DBの中にある**わけです。

---

## 15.3 スキーマ履歴トピックを先に作る

コネクタを登録する前に、ひとつだけ手動でトピックを作ります。

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic schema-history.shop \
  --partitions 1 --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=-1
```

### なぜ必要か

Debezium は、binlog を読むために**「そのイベントが起きた時点でのテーブル定義」を知っている必要があります**。

これは意外に思えるかもしれませんが、理由は単純です。**binlog の行データには列名が入っていません**。「1列目に98000、2列目に1」という値の並びしか記録されていないのです。列名を復元するには、その時点の `CREATE TABLE` を知っていなければなりません。

しかも、途中で `ALTER TABLE` されているかもしれません。だから Debezium は、**DDLの履歴を時系列で丸ごと**このトピックに保存し、再起動時にそれを最初から読み直してテーブル定義を再構築します。

### だから圧縮してはいけない

第14章で「コンパクションはキーごとに最新1件を残す」と学びました。もしこのトピックを圧縮すると、**途中のDDLが消えてしまい、過去の位置から再開できなくなります**。

上のコマンドで指定した2つの設定はそのためのものです。

- `cleanup.policy=delete`：圧縮しない
- `retention.ms=-1`：**無期限保持**（時間で消えないようにする）

> ⚠️ このトピックを消してしまうと、コネクタは「テーブル定義がわからない」と言って起動できなくなります。復旧するには、コネクタを削除して初期スナップショットからやり直すことになります。運用では**バックアップ対象**として扱ってください。

---

## 15.4 Debezium コネクタを登録する

### 設定JSONを読む

`register-mysql-source.json` の中身を、ブロックごとに解説します。

```json
{
  "name": "products-source",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
```

コネクタ名 `products-source` は、あとで状態確認や削除に使う識別子です。

```json
    "database.hostname": "mysql",
    "database.port": "3306",
    "database.user": "debezium",
    "database.password": "dbzpw",
    "driver.allowPublicKeyRetrieval": "true",
```

最後の1行が**MySQL 8系の最大の罠**です。MySQL 8 は既定の認証方式が `caching_sha2_password` になっており、TLSなしで接続する場合、クライアントはサーバーの公開鍵を取得する必要があります。これを許可しないと、

```
Public Key Retrieval is not allowed
```

というエラーで接続に失敗します。**学習用にTLSを無効にしている以上、この設定は必須**です（本番ではTLSを使い、この設定は外します）。

```json
    "database.server.id": "184054",
```

**Debezium 自身がレプリカとして名乗る番号**です。MySQL 本体の `server-id=1` とも、他のレプリカとも重複させてはいけません。重複すると、両者が binlog の接続を奪い合い、`A slave with the same server_uuid/server_id as this slave has connected` というエラーで切断を繰り返します。

```json
    "topic.prefix": "shop",
    "database.include.list": "shopdb",
    "table.include.list": "shopdb.products",
```

トピック名は **`topic.prefix` . `DB名` . `テーブル名`** の形で自動的に決まります。つまり今回は `shop.shopdb.products` です。第13章の構成図で予告したとおりですね。

`table.include.list` で対象を絞っていますが、これは**とても重要**です。指定しないと、そのDBの全テーブルがCDC対象になり、意図しないトピックが大量に生まれます。

```json
    "snapshot.mode": "initial",
```

初回起動時に既存の全行を読み出すモードです（15.5で詳述）。

```json
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false"
  }
}
```

Debezium は既定で、JSONの中に**スキーマ定義（各フィールドの型情報）を丸ごと同梱**します。正確ですが、1件のイベントが数百行になり、学習には向きません。ここでは無効にして、素の JSON を出力させます。

> 💡 **本番では Avro + Schema Registry が定番**
> スキーマを毎回同梱するのは無駄が多く、かといって無効にすると型情報が失われます。実務では **Avro 形式 + Schema Registry**（スキーマを別サーバーで管理し、メッセージにはIDだけ入れる）を使うのが定石です。メッセージサイズが劇的に小さくなり、スキーマ変更の互換性チェックも自動化できます。本教科書では構成を増やさないため JSON のままいきます。

### 登録する

```bash
curl -i -X POST -H "Content-Type: application/json" \
  --data @phase3-15/register-mysql-source.json \
  http://localhost:8083/connectors
```

`HTTP/1.1 201 Created` が返れば登録成功です。

### 状態を確認する

```bash
curl -s http://localhost:8083/connectors/products-source/status | jq
```

```json
{
  "name": "products-source",
  "connector": { "state": "RUNNING", "worker_id": "..." },
  "tasks": [ { "id": 0, "state": "RUNNING", "worker_id": "..." } ],
  "type": "source"
}
```

**`connector` と `tasks` の両方が `RUNNING`** であることを必ず確認してください。よくあるのが「コネクタは RUNNING だがタスクが FAILED」というパターンで、この場合イベントは一切流れません。

失敗しているときは、同じ出力の中に `trace` としてスタックトレースが入っています。

```bash
curl -s http://localhost:8083/connectors/products-source/status | jq -r '.tasks[0].trace' | head -n 20
```

**よく使うREST API**

| 操作 | コマンド |
|---|---|
| 一覧 | `curl -s localhost:8083/connectors` |
| 状態 | `curl -s localhost:8083/connectors/{name}/status` |
| 設定確認 | `curl -s localhost:8083/connectors/{name}/config` |
| 再起動（タスク） | `curl -X POST localhost:8083/connectors/{name}/tasks/0/restart` |
| 一時停止 / 再開 | `curl -X PUT localhost:8083/connectors/{name}/pause`（`/resume`） |
| 削除 | `curl -X DELETE localhost:8083/connectors/{name}` |

---

## 15.5 初期スナップショット ― 「今すでにある6件」はどうなるのか

ここで素朴な疑問が湧きます。**CDCはこれから起きる変更を捕まえる仕組みなのに、すでにテーブルに入っている6件はどうやってESに届くのでしょうか？**

答えが **初期スナップショット（initial snapshot）** です。コネクタは初回起動時に、次の順序で動きます。

```
 1. binlog の現在位置を記録する        ← 「ここから先が"変更"」という基準点
 2. 対象テーブルの CREATE TABLE を読み、スキーマ履歴トピックに保存
 3. SELECT * FROM products を実行し、
    全行を op:"r"（read）のイベントとして Kafka に流す
 4. 1で記録した位置から binlog の読み取りを開始する
       → 以降は op:"c" / "u" / "d" がリアルタイムで流れる
```

つまり、**「現在の全体像」＋「以後の差分」** という形で、下流は完全な状態を再現できます。第13章で「全件再構築」と「CDC」を対比しましたが、実は CDC の中に全件読み込みが組み込まれているわけです。

`op` の値は4種類です。

| `op` | 意味 | いつ出るか |
|---|---|---|
| `r` | read | 初期スナップショット |
| `c` | create | INSERT |
| `u` | update | UPDATE |
| `d` | delete | DELETE |

主な `snapshot.mode`（第17章の再同期で再登場します）。

| モード | 動作 |
|---|---|
| `initial` | **既定**。初回のみスナップショット、以降は binlog |
| `no_data` | スキーマだけ読み、データは読まない（今後の変更のみ欲しいとき） |
| `when_needed` | 必要と判断したときに自動でスナップショットし直す |
| `never` | スナップショットせず binlog の現在位置から |

---

## 15.6 ハンズオン ― 変更イベントを見る

### スナップショットの結果を見る

コネクタが自動でトピックを作っているはずです。

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

```
connect_configs
connect_offsets
connect_statuses
schema-history.shop
shop.shopdb.products      ← これ
```

中身を最初から読みます。

```bash
docker compose exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic shop.shopdb.products \
  --from-beginning \
  --property print.key=true
```

6件のイベントが流れてきます。1件を整形するとこうなっています（見やすさのため一部省略）。

```json
{"id":1}    ← キー（主キーがそのまま入る）
{
  "before": null,
  "after": {
    "id": 1,
    "name": "軽量ノートパソコン 14インチ",
    "description": "重量1.1kgの薄型ノートPC。...",
    "category": "pc",
    "tags": "軽量,14インチ,モバイル",
    "price": 98000,
    "in_stock": 1,
    "updated_at": "2026-08-03T09:12:00Z"
  },
  "source": {
    "connector": "mysql", "db": "shopdb", "table": "products",
    "server_id": 0, "file": "binlog.000002", "pos": 1571,
    "snapshot": "true", "ts_ms": 1785...
  },
  "op": "r",
  "ts_ms": 1785...
}
```

**確認ポイント**

- `op` が `"r"` ＝ スナップショットで読まれた行
- `before` が `null` ＝ スナップショットには「変更前」が存在しない
- **キーが `{"id":1}`** ＝ 第14章で学んだとおり、同じ商品の変更は必ず同じパーティションに入る
- `in_stock` が `1`（`true` ではない）＝ MySQL の `BOOLEAN` は実体が `TINYINT(1)`。**ES側で `boolean` にするには変換が要る**（第16章で扱います）

### いよいよ本番 ― UPDATE を流す

コンシューマは**起動したまま**にして、別のターミナルを開いてください。

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb \
  -e "UPDATE products SET price = 89000 WHERE id = 1;"
```

コンシューマ側の画面に、1秒以内に新しいイベントが現れます。

```json
{"id":1}
{
  "before": { "id":1, "name":"軽量ノートパソコン 14インチ", "price":98000, ... },
  "after":  { "id":1, "name":"軽量ノートパソコン 14インチ", "price":89000, ... },
  "source": { "file":"binlog.000002", "pos":2841, "snapshot":"false", ... },
  "op": "u",
  "ts_ms": ...
}
```

**これがCDCです。** アプリケーションには一行もコードを書いていません。管理画面からの更新でも、バッチからの更新でも、いま打ったような手動SQLでも、**MySQLにコミットされた変更はすべて例外なくここに流れてきます**。第13章の「罠③：書き込み経路すべてに実装が必要」が、根本から解決されているのが分かるはずです。

### INSERT と DELETE も試す

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb -e "
INSERT INTO products (name, description, category, tags, price, in_stock)
VALUES ('モバイルバッテリー 20000mAh', '大容量タイプ。PD対応で高速充電。',
        'accessory', 'バッテリー,PD,大容量', 5980, TRUE);
"
```

→ `op: "c"`、`before: null` のイベントが流れます。

```bash
docker compose exec mysql mysql -uroot -prootpw shopdb -e "DELETE FROM products WHERE id = 7;"
```

→ **2件**のメッセージが流れます。

```json
{"id":7}
{ "before": {...消える前の値...}, "after": null, "op": "d", ... }

{"id":7}
null                    ← ★ これが tombstone
```

第13章の論点2、第14章のログコンパクションで学んだ **tombstone** が、実物として出てきました。この `null` を Elasticsearch の `DELETE` に変換するのが、次章の主役の一つです。

---

## 15.7 つまずいたときの切り分け

CDCは登場人物が多いぶん、「どこで止まっているか」を切り分ける技術が重要です。**上流から順に**見ていきます。

```
 ① MySQLでbinlogが出ているか
      docker compose exec mysql mysql -uroot -prootpw -e "SHOW BINARY LOG STATUS\G"
      （MySQL 8.4以降。8.0までは SHOW MASTER STATUS）
      → 更新のたびに Position が増えていればOK

 ② Debeziumが接続できているか
      docker compose exec mysql mysql -uroot -prootpw -e "SHOW REPLICAS;"
      → Debezium が server_id 184054 のレプリカとして表示される

 ③ コネクタが動いているか
      curl -s localhost:8083/connectors/products-source/status | jq

 ④ Kafkaに届いているか
      kafka-console-consumer.sh --from-beginning ...

 ⑤ Connectのログ
      docker compose logs -f connect | tail -n 50
```

**よくあるエラーと対処**

| 症状 | 原因 | 対処 |
|---|---|---|
| `Public Key Retrieval is not allowed` | MySQL 8 の認証方式 | `driver.allowPublicKeyRetrieval: "true"` を追加 |
| `Access denied for user 'debezium'` | 権限不足 | 15.2 の `GRANT` を確認 |
| `A slave with the same server_uuid/server_id ...` | `server-id` の重複 | `database.server.id` を他と重複しない値に |
| タスクが FAILED、`Cannot find schema history topic` | 履歴トピックを消した | コネクタを削除し、再スナップショット |
| イベントが全く流れない・トピックがない | ブローカーの `auto.create.topics.enable=false` とタスク失敗 | まず `status` の `trace` を読む |
| `The db history topic is missing` | 履歴トピックの retention 切れ | `retention.ms=-1` を確認 |

> 💡 **コラム：コネクタを「作り直す」ときの注意**
> 学習中は何度もコネクタを作り直すことになります。`DELETE /connectors/products-source` で削除しても、**Kafka に残っているオフセットとスキーマ履歴、そしてトピックのメッセージは消えません**。同じ名前で再登録すると「続きから」再開してしまい、スナップショットが走らずに戸惑うことがあります。
> 完全にまっさらから試したいときは、次のいずれかにしてください。
> - コネクタ名と `topic.prefix`、`schema.history.internal.kafka.topic` を別の名前に変える
> - `docker compose down` で環境ごと作り直す（Kafkaを永続化していないので全部消えます）

---

## まとめ

- **Kafka Connect** は、ワーカー（プロセス）／コネクタ（設定）／タスク（実行単位）の3層構造。設定も進捗も Kafka の内部トピックに保存されるため、再起動しても続きから動く
- **Debezium MySQL コネクタのタスクは常に1つ**。binlog は1本のストリームなので分割できない
- MySQL側に必要なのは、`server-id`・binlog有効化・`binlog_row_image=FULL`・CDC用ユーザーへの `REPLICATION SLAVE` 等の権限。**`binlog_format` は 8.0.34 以降 非推奨で、既定が ROW なので書かない**
- MySQL 8 では `driver.allowPublicKeyRetrieval=true` がないと接続できない（TLSなしの場合）
- **スキーマ履歴トピック**は、binlog に列名が入っていないため必須。**圧縮せず・無期限保持**で作る
- **初期スナップショット**が既存の全行を `op:"r"` として流し、その後 binlog 追従に切り替わる。「現在の全体像＋以後の差分」で下流は完全な状態を再現できる
- 削除時は **`op:"d"` のイベントと tombstone（値が `null`）の2件**が流れる
- 切り分けは必ず**上流から**。MySQL → Debezium接続 → コネクタstatus → Kafka の順に見る

---

## 理解度チェック

**問1.** Debezium が「スキーマ履歴トピック」を必要とするのはなぜですか。また、このトピックに `cleanup.policy=compact` を設定してはいけないのはなぜですか。

**問2.** `database.server.id` には、MySQL 本体の `server-id` と異なる値を指定します。なぜですか。

**問3.** コネクタを新規登録した直後、`shop.shopdb.products` トピックには6件のイベントが入っていました。この6件の `op` は何で、`before` はどうなっていますか。またこの仕組みは何と呼ばれますか。

**問4.** `curl .../status` の結果、`connector.state` は `RUNNING` ですが `tasks[0].state` が `FAILED` でした。この状態でイベントは流れますか。次に何を見るべきですか。

**問5.** MySQL で `DELETE FROM products WHERE id = 7;` を実行すると、Kafka には何件のメッセージが流れますか。それぞれの内容と役割を説明してください。

<details>
<summary>解答を見る</summary>

**問1.**
binlog の行データには**列名が含まれておらず、値の並びしか記録されていない**ため。列名を復元するには、その時点のテーブル定義（`CREATE TABLE` / `ALTER TABLE` の履歴）が必要になる。Debezium は再起動時にこのトピックを最初から読み直してテーブル定義を再構築する。
圧縮してはいけない理由：コンパクションはキーごとに最新1件しか残さないため、**途中のDDL履歴が失われ、過去の binlog 位置から再開できなくなる**から。`cleanup.policy=delete` かつ `retention.ms=-1`（無期限）で作る。

**問2.**
Debezium は MySQL に対して**1台のレプリカとして接続する**ため、レプリケーションに参加する各サーバーは一意な `server-id` を持つ必要があるから。重複すると、同じIDのレプリカ同士が binlog 接続を奪い合い、切断と再接続を繰り返す。

**問3.**
`op` はすべて `"r"`（read）、`before` はすべて `null`。
これは **初期スナップショット（initial snapshot）** で、コネクタが binlog の現在位置を記録したうえで `SELECT` により既存の全行を読み出したもの。以後は binlog を読んで `c` / `u` / `d` が流れる。

**問4.**
**流れません。** 実際にデータを運ぶのはタスクであり、コネクタが RUNNING でもタスクが FAILED なら何も処理されない。
次に見るべきは、同じ `status` レスポンスに含まれる `tasks[0].trace`（スタックトレース）と、`docker compose logs connect` のログ。原因を直したら `POST /connectors/{name}/tasks/0/restart` で再起動する。

**問5.**
**2件**。
1. `op: "d"` のイベント。`before` に削除前の行の内容が入り、`after` は `null`。「この行が削除された」という事実と、消える前の値を伝える
2. **tombstone**。キーは同じ `{"id":7}` で、値が `null`。Kafka のログコンパクションに対して「このキーの過去のレコードはすべて削除してよい」と伝えるマーカーであり、下流のシンクにとっては「対象を削除せよ」の合図になる

</details>

---

## 次の章へ

上流ができました。次章はいよいよ**出口**、Kafka から Elasticsearch への接続です。

ここでやることは、単に「つなぐ」だけではありません。**`_id` に何を使うか**（冪等性の鍵）、**tombstone をどう削除に変換するか**、**`tags` のカンマ区切り文字列をどう配列にするか**、**`in_stock` の `1` をどう `true` にするか** ―― CDCイベントとESドキュメントの「形の違い」を埋める作業が待っています。ここを乗り越えると、フェーズ1で手作業だった `bulk` 投入が、完全に自動化されます。

> **次章：第16章「Kafka から Elasticsearch へ ― シンク構築と tombstone による削除」**

---

### この章のキーワード

Kafka Connect / ワーカー / コネクタ / タスク / `tasks.max` / プラグイン / `connect_configs` / `connect_offsets` / REST API / Debezium MySQL Connector / `server-id` / `binlog_row_image` / GTID / `REPLICATION SLAVE` 権限 / `caching_sha2_password` / `allowPublicKeyRetrieval` / `topic.prefix` / `table.include.list` / スキーマ履歴トピック / `retention.ms=-1` / 初期スナップショット / `snapshot.mode` / `op`（r / c / u / d）/ tombstone / コネクタstatus / trace
