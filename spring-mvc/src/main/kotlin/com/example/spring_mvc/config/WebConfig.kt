package com.example.spring_mvc.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 *  Corsの設定を行うためにクラスを継承している
 */

@Configuration
@EnableWebMvc
class WebConfig: WebMvcConfigurer {


    // @CorsOriginアノテーションを使う方法もあるが、こちらの方が柔軟に設定できる
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
    }

    // APIリクエスト・レスポンス時のシリアライズ・デジリアライズの設定
    @Bean
    fun objectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder.json()
            .indentOutput(true) // JSONを見やすく整形する
            .build()

    }
}