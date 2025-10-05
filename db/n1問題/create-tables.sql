CREATE TABLE users (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE posts (
    id INT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users (id, name, email) VALUES
(1, '田中 太郎', 'taro.tanaka@example.com'),
(2, '佐藤 花子', 'hanako.sato@example.com');

INSERT INTO posts (id, user_id, title, content) VALUES
(1, 1, 'N+1問題とは？', 'データベースアクセスのパフォーマンスについて解説します。'),
(2, 2, '初めてのSQL', 'CREATE TABLEとINSERT文の基本。'),
(3, 1, '効率的なクエリ', 'JOINを使ったデータ取得のテクニック。'),
(4, 2, 'プログラミングの基礎', '変数とループ処理について。'),
(5, 1, '今日のニュース', '最新のテクノロジー動向。');