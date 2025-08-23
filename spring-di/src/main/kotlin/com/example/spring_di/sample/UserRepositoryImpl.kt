package com.example.spring_di.sample

import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl : UserRepository {
    override fun countByUsername(username: String): Int {
        println("ユーザーの数をレスポンスしまっせ！")
        return 100
    }

    override fun save(user: UserService.User) {
       println("ユーザー情報を登録しまっせ！")
    }
}