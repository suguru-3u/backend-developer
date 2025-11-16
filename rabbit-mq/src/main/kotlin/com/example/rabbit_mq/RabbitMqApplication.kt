package com.example.rabbit_mq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RabbitMqApplication

fun main(args: Array<String>) {
	runApplication<RabbitMqApplication>(*args)
}
