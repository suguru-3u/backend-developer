package com.example.rabbit_mq

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

@Configuration
class RabbitMQConfig {

    companion object {
        // --- メインキューとEXCHANGE ---
        const val MAIN_QUEUE_NAME = "my-queue"
        const val MAIN_EXCHANGE_NAME = "my-exchange"
        const val MAIN_ROUTING_KEY = "my.routing.key"

        // --- DLQ関連 ---
        const val DLQ_NAME = "dead-letter-queue"
        const val DLX_NAME = "dead-letter-exchange"
        // DLQへのルーティングキー（DLXとDLQをバインドするために使用）
        const val DL_ROUTING_KEY = "dead.message"
    }

    // ----------------------------------------------------
    // 1. DLQとDLXの定義
    // ----------------------------------------------------

    @Bean
    fun dlq(): Queue {
        // DLQは特別な引数は不要、通常の耐久性のあるキューとして定義
        return Queue(DLQ_NAME, true)
    }

    @Bean
    fun dlx(): DirectExchange {
        // DLXはDirect Exchangeとして定義
        return DirectExchange(DLX_NAME)
    }

    @Bean
    fun dlqBinding(dlq: Queue, dlx: DirectExchange): Binding {
        // DLQをDLXにバインド
        return BindingBuilder.bind(dlq).to(dlx).with(DL_ROUTING_KEY)
    }

    // ----------------------------------------------------
    // 2. メインキューとメインEXCHANGEの定義
    // ----------------------------------------------------

    @Bean
    fun mainQueue(): Queue {
        // Main QueueにDLXの設定を追加
        return QueueBuilder.durable(MAIN_QUEUE_NAME)
            .withArgument("x-dead-letter-exchange", DLX_NAME) // 処理失敗時の転送先EXCHANGEを指定
            .withArgument("x-dead-letter-routing-key", DL_ROUTING_KEY) // 転送時に使用するルーティングキーを指定
            .build()
    }

    @Bean
    fun mainExchange(): DirectExchange {
        return DirectExchange(MAIN_EXCHANGE_NAME)
    }

    @Bean
    fun mainBinding(mainQueue: Queue, mainExchange: DirectExchange): Binding {
        // Main QueueをMain Exchangeにバインド
        return BindingBuilder.bind(mainQueue).to(mainExchange).with(MAIN_ROUTING_KEY)
    }

    // ----------------------------------------------------
    // 3. リスナーコンテナファクトリの定義 (リトライとDLQ転送設定)
    // ----------------------------------------------------

    /**
     * リトライとエラーハンドリングを設定したリスナーコンテナファクトリを定義します。
     * これにより、アプリケーション側での無限リトライを防ぎ、DLQへメッセージを転送します。
     */
    @Bean
    fun dlqRetryContainerFactory(
        connectionFactory: ConnectionFactory,
        rabbitProperties: RabbitProperties
    ): SimpleRabbitListenerContainerFactory {

        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)

        factory.setAdviceChain(
            RetryInterceptorBuilder.stateless()
                .maxAttempts(5) // 最大リトライ回数
                .backOffOptions(
                    1000,   // 初回の待機 1秒
                    2.0,    // 2倍ずつ増えていく
                    10000   // 最大待機時間 10秒
                )
                .recoverer(RejectAndDontRequeueRecoverer()) // 失敗→DLQへ
                .build()
        )

        return factory
    }
}