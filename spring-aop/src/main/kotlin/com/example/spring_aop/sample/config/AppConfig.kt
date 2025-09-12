package com.example.spring_aop.sample.config

import com.example.spring_aop.sample.PasswordEncoder
import com.example.spring_aop.sample.PasswordEncoderImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.PropertySource

/**
 * クラスにアノテーションを付与するのではなく、DIを管理するクラスで定義する方法
 */
@ComponentScan(basePackages = ["com.example.spring_aop.sample"])
@Configuration
@EnableAspectJAutoProxy // このアノテーションがないとAOPが実行されない
@PropertySource("classpath:application.properties")
class AppConfig(
    @Value("\${sample.stringName}")
    val sample1: String,
) {
    @Bean(name = ["PasswordEncoderImpl"])
    fun PasswordEncoder(): PasswordEncoder {
        println("application.propertiesの値： $sample1")
        return PasswordEncoderImpl()
    }
}