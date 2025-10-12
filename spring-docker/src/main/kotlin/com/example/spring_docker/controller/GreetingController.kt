package com.example.spring_docker.controller

import java.util.concurrent.atomic.AtomicLong

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GreetingController {

    companion object {
        private const val template = "Hello, %s!　Yes!!!!!!!!!!"
    }
    private val counter = AtomicLong()

    @GetMapping("/greeting")
    fun greeting(@RequestParam name: String = "World") =
        Greeting(counter.incrementAndGet(), String.format(template, name))

    data class Greeting(val id: Long, val content: String)

}
