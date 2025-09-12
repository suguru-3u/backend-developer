package com.example.spring_aop.sample

import com.example.spring_aop.sample.config.AppProps
import org.springframework.stereotype.Component

@Component
class PasswordEncoderImpl: PasswordEncoder {
    override fun encode(rawPassword: String) {
        println("$rawPassword :をパスワードエンコードしまっせ！")
    }
}