DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `users` (`id`, `username`, `email`, `created_at`) VALUES
(1, 'Alice', 'alice@example.com', '2025-10-01 10:00:00'),
(2, 'Bob', 'bob@example.com', '2025-10-02 11:30:00'),
(3, 'Charlie', 'charlie@example.com', '2025-10-03 15:45:00');