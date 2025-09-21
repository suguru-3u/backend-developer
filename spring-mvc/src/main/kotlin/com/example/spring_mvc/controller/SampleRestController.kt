package com.example.spring_mvc.controller

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono // bodyToMonoを使う場合も必要です
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicLong

@RestController
@RequestMapping("/api")
class SampleRestController {

    companion object {
        private const val template = "Hello, %s!"
    }

    private val counter = AtomicLong()

    @GetMapping("/greeting")
    fun greeting(@RequestParam name: String = "World"): Greeting {
        return Greeting(counter.incrementAndGet(), String.format(template, name))
    }

    @GetMapping("/error")
    fun error(): String {
        throw Exception("This is a test exception")
    }

    // RestTemplateの代わりにWebClientを使う例
    @GetMapping("/webClient")
    fun webClient(): String {
        // WebClientのインスタンスを作成
        val webClient = WebClient.create("https://api.example.com")

        // GETリクエストを送信し、レスポンスを非同期で受け取る
        val mono = webClient.get()
            .uri("/users/{id}", 123)
            .retrieve() // レスポンスの取得を開始
            .bodyToMono<User>() // レスポンスボディをUserオブジェクトに変換
            .block() // ブロックしてレスポンスを待つ（非推奨、例示のために使用）

        // Monoの購読
        mono?.let {
            println("User ID: ${it.id}, Name: ${it.name}, Email: ${it.email}")
        }
        return "WebClient request sent"
    }

    data class User(val id: Long, val name: String, val email: String)

    data class Greeting(val id: Long, val content: String)
}