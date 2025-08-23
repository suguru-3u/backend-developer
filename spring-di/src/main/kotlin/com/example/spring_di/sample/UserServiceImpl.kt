package com.example.spring_di.sample

import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {
    override fun register(user: UserService.User, rawPassword: String) {
        println("サービス層のユーザー登録処理です")
    }
}