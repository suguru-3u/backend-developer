package com.example.spring_jdbc

import com.example.spring_jdbc.domain.User
import com.example.spring_jdbc.repository.SampleRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringJdbcApplication

fun main(args: Array<String>) {
    runApplication<SpringJdbcApplication>(*args)
}
