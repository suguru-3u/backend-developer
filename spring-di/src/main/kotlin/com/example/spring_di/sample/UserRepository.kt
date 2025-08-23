package com.example.spring_di.sample

interface UserRepository {
    // ユーザー情報を永続化層に保存する
    fun save(user: UserService.User)

    // ユーザー数をカウントする
    fun countByUsername(username: String): Int
}