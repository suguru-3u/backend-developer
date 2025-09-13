package com.example.spring_jdbc.config

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

// CommandLineRunner: アプリケーション起動後に実行したい処理を実装するためのインターフェース
@Component
class AppLoader : CommandLineRunner {

    override fun run(vararg args: String?) {
        println("Appの起動だぜ！！！")
    }
}