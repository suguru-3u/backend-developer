package com.example.spring_di.sample

interface PasswordEncoder {
    // パスワードをハッシュ化する処理
    fun encode(rawPassword: String);
}