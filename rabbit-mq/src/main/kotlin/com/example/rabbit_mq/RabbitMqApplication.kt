package com.example.rabbit_mq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class RabbitMqApplication

fun main(args: Array<String>) {
	runApplication<RabbitMqApplication>(*args)
}
