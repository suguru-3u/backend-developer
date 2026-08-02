-- ============================================================
--  第18章：正規化されたスキーマを追加する
--   実行: docker compose exec -T mysql mysql -uroot -prootpw shopdb < phase3-18/01-categories.sql
-- ============================================================

-- ---------- カテゴリマスタ ----------
CREATE TABLE categories (
  code          VARCHAR(50) PRIMARY KEY,   -- products.category と対応
  name          VARCHAR(100) NOT NULL,     -- 画面表示名
  display_order INT NOT NULL DEFAULT 0,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                         ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO categories (code, name, display_order) VALUES
  ('pc',        'パソコン',       1),
  ('monitor',   'モニター',       2),
  ('accessory', '周辺機器',       3),
  ('audio',     'オーディオ',     4);

-- ---------- Outbox テーブル（18.6で使用） ----------
CREATE TABLE outbox (
  id           CHAR(36)     PRIMARY KEY,
  aggregatetype VARCHAR(64) NOT NULL,   -- 例: product
  aggregateid   VARCHAR(64) NOT NULL,   -- 例: 1
  type          VARCHAR(64) NOT NULL,   -- 例: ProductUpserted
  payload       JSON        NOT NULL,   -- ESに入れたい完成形のJSON
  created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
