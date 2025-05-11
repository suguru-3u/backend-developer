CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    role       ENUM('USER', 'ADMIN') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 平文: password123
-- 平文: admin456
-- 平文: ichiro789

INSERT INTO users (email, password, role)
VALUES
    ('taro@example.com', '$2a$10$fbH0TGe8it4vYoEGp7R8vuw3OExv3bnEz9KDWQ0d8mccZKLuNwP5S', 'USER'),
    ('hanako@example.com', '$2a$10$KxCh62wq8N0/RKfgYwPi7O7BC2H1Pr6ATUpZYALJDYB7yKynsCxyO', 'ADMIN'),
    ('ichiro@example.com', '$2a$10$1Xx3W6Yxkg4fELPoUpt9ceRpRYe/hn3ZmcU1xrrIPmC4z9nER1mmW', 'USER');
