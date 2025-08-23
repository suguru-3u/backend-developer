package com.example.spring_di.sample

interface UserService {
    // ユーザー情報を登録する処理
    fun register(user: User, rawPassword: String);

    class User(val name: String)
}