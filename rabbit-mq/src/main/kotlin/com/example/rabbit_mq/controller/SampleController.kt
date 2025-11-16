package com.example.rabbit_mq.controller

import com.example.rabbit_mq.service.RabbitMQProducerService
import org.springframework.web.bind.annotation.*

@RestController
class SampleController(
    private val rabbitMQProducerService: RabbitMQProducerService,
) {

    @RequestMapping("/rabbit-mq-success", method = [RequestMethod.GET])
    fun success() {
        println("成功リクエストを検知")
        return rabbitMQProducerService.sendMessage("Hello Rabbit MQ")
    }

    @RequestMapping("/rabbit-mq-fail", method = [RequestMethod.GET])
    fun fail() {
        println("失敗リクエストを検知")
        return rabbitMQProducerService.sendMessage("Hello Rabbit FAIL")
    }
}