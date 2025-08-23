package com.example.spring_di.sample.config

import com.example.spring_di.sample.PasswordEncoder
import com.example.spring_di.sample.PasswordEncoderImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * クラスにアノテーションを付与するのではなく、DIを管理するクラスで定義する方法
 */
@ComponentScan(basePackages = ["com.example.spring_di.sample"])
@Configuration
class AppConfig {

    @Bean(name = ["PasswordEncoderImpl"])
    fun PasswordEncoder(): PasswordEncoder {
        return PasswordEncoderImpl()
    }
}