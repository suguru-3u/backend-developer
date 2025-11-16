package com.example.rabbit_mq.service

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

/**
 * このクラスは、送信側（パブリッシャー）の処理を記載しています。
 */

@Service
class RabbitMQProducerService(private val rabbitTemplate: RabbitTemplate) {

    // エクスチェンジ名とルーティングキーを設定
    private val EXCHANGE_NAME = "my-exchange"
    private val ROUTING_KEY = "my.routing.key"

    fun sendMessage(message: String) {
        println("Sending message to RabbitMQ: $message")
        // メッセージをエクスチェンジにルーティングキー付きで送信
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message)
    }
}