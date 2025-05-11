package com.example.graphQL.controller

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class SecondController {
    @QueryMapping
    fun helloSecond(): String {
        return "こんにちは、SecondController GraphQL！"
    }
}