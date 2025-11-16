package com.example.rabbit_mq.service

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class RabbitMQConsumerService {
    // 'my-queue'という名前のキューのメッセージをリッスン
    @RabbitListener(queues = ["my-queue"])
    fun listen(message: String) {
        println("Received message from RabbitMQ: $message")

        // 【DLQへの転送をテストするための失敗ロジック】
        if (message.contains("FAIL")) {
            println("--- FAILED: Message contains 'FAIL'. Throwing exception to trigger DLX routing. ---")
            // 例外をスローすると、メッセージはNackされ、リトライロジックが働きます。
            throw RuntimeException("Simulated processing failure for message: $message")
        }

        // 成功時の処理
        println("--- SUCCESS: Message processed successfully. ---")
    }

    // DLQのメッセージを監視するコンシューマー (オプション)
    @RabbitListener(queues = ["dead-letter-queue"])
    fun listenDlq(message: String) {
        println("--- DLQ RECEIVED: This message failed after all retries! ---")
        println("DLQ Content: $message")
    }
}