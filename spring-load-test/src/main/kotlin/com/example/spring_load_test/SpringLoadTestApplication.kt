package com.example.spring_load_test

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringLoadTestApplication

fun main(args: Array<String>) {
	runApplication<SpringLoadTestApplication>(*args)
}
