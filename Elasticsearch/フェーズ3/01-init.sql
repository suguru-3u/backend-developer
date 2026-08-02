-- ============================================================
--  shopdb 初期化スクリプト
--  MySQLコンテナの初回起動時に自動実行されます
--  （/docker-entrypoint-initdb.d にマウント）
-- ============================================================

USE shopdb;

-- ------------------------------------------------------------
--  商品テーブル
--   ※ tags は第18章で正規化します。ここではあえてカンマ区切り
-- ------------------------------------------------------------
CREATE TABLE products (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(200)  NOT NULL,
  description TEXT,
  category    VARCHAR(50)   NOT NULL,
  tags        VARCHAR(255),
  price       INT           NOT NULL,
  in_stock    BOOLEAN       NOT NULL DEFAULT TRUE,
  updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO products (name, description, category, tags, price, in_stock) VALUES
  ('軽量ノートパソコン 14インチ',
   '重量1.1kgの薄型ノートPC。長時間バッテリーで持ち運びに最適です。',
   'pc',        '軽量,14インチ,モバイル',      98000, TRUE),
  ('ワイヤレスマウス 静音タイプ',
   'クリック音を抑えた静音設計。オフィスや図書館でも気兼ねなく使えます。',
   'accessory', '静音,ワイヤレス,マウス',       3200, TRUE),
  ('メカニカルキーボード 日本語配列',
   '打鍵感にこだわった赤軸メカニカルキーボード。日本語108キー配列。',
   'accessory', 'メカニカル,日本語配列,赤軸',  12800, TRUE),
  ('4Kモニター 27インチ',
   '高精細4K表示に対応した27インチディスプレイ。写真編集にも十分な色域。',
   'monitor',   '4K,27インチ,IPS',             45800, FALSE),
  ('USB-Cハブ 7ポート',
   'HDMI・SD・USB-Aをまとめて増設できる7ポートハブ。給電にも対応。',
   'accessory', 'USB-C,ハブ,7ポート',           4980, TRUE),
  ('ノートPCスタンド アルミ製',
   '角度を6段階に調整できるアルミ製スタンド。放熱性にも優れています。',
   'accessory', 'スタンド,アルミ,放熱',         3980, TRUE);

-- ------------------------------------------------------------
--  Debezium 用ユーザー
--   必要な権限（Debezium公式が要求する最小構成）
--    SELECT             : 初期スナップショットで全行を読む
--    RELOAD             : スナップショット時のフラッシュ
--    SHOW DATABASES     : 対象DBの列挙
--    REPLICATION SLAVE  : binlog をレプリカとして受信する
--    REPLICATION CLIENT : binlog の現在位置を問い合わせる
--    LOCK TABLES        : スナップショット時のテーブルロック（環境により必要）
-- ------------------------------------------------------------
CREATE USER 'debezium'@'%' IDENTIFIED BY 'dbzpw';

GRANT SELECT, RELOAD, SHOW DATABASES,
      REPLICATION SLAVE, REPLICATION CLIENT, LOCK TABLES
  ON *.* TO 'debezium'@'%';

FLUSH PRIVILEGES;
