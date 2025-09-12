package com.example.spring_aop.sample

interface PasswordEncoder {
    // パスワードをハッシュ化する処理
    fun encode(rawPassword: String);
}