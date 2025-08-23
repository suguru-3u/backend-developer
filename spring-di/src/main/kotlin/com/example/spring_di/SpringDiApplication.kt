package com.example.spring_di

import com.example.spring_di.sample.PasswordEncoder
import com.example.spring_di.sample.config.AppConfig
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext

@SpringBootApplication
class SpringDiApplication

fun main(args: Array<String>) {
    val appleContext = runApplication<SpringDiApplication>(*args)
    val context = AnnotationConfigApplicationContext(AppConfig::class.java)
    val passwordEncoder = context.getBean<PasswordEncoder>("PasswordEncoderImpl")
    passwordEncoder.encode("sample-password1")

    // BCryptPasswordEncoderはAppConfigでDI登録していないが、@ComponentのアノテーションでDI登録を行っている
    // @ComponentのアノテーションでDI登録しているので、runApplicationからBeanを取得する
    // @ComponentScan(basePackages = ["com.example.spring_di.sample"])をAppConfigに設定すれば、contextから取得できる
    //    val bCryptPasswordEncoder = appleContext.getBean<PasswordEncoder>("BCryptPasswordEncoder")
    val bCryptPasswordEncoder = context.getBean<PasswordEncoder>("BCryptPasswordEncoder")
    bCryptPasswordEncoder.encode("sample-password2")
}
