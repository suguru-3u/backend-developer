package com.example.spring_di.sample

import org.springframework.stereotype.Component

@Component("BCryptPasswordEncoder")
class BCryptPasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: String) {
        println("$rawPassword :をBCryptPasswordEncoderでエンコードしまっせ！")
    }
}