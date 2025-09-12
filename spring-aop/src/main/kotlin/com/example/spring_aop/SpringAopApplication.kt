package com.example.spring_aop

import com.example.spring_aop.sample.PasswordEncoder
import com.example.spring_aop.sample.config.AppConfig
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext

@SpringBootApplication
class SpringAopApplication

fun main(args: Array<String>) {
	runApplication<SpringAopApplication>(*args)
	val context = AnnotationConfigApplicationContext(AppConfig::class.java)
	val passwordEncoder = context.getBean<PasswordEncoder>("PasswordEncoderImpl")
	passwordEncoder.encode("sample-password1")
}
