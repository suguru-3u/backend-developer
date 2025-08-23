package com.example.spring_di.sample

class PasswordEncoderImpl : PasswordEncoder {
    override fun encode(rawPassword: String) {
        println("$rawPassword :をパスワードエンコードしまっせ！")
    }
}