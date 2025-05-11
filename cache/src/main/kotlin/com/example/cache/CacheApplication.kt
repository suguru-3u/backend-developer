package com.example.cache

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CacheApplication

fun main(args: Array<String>) {
	runApplication<CacheApplication>(*args)
}
