package com.example.spring_di.sample.contoroller

import com.example.spring_di.sample.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

    // コンストラクタで定義することが推奨されているが、あえてSetterでインジェクションしてみた
    private lateinit var userService: UserService

    @Autowired
    fun setUserService(userService: UserService) {
        this.userService = userService
    }

    @GetMapping
    fun sample() {
        userService.register(
            user = UserService.User(
                name = "Sample"
            ),
            rawPassword = "sample-password3"
        )
    }

}