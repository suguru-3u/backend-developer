package com.example.graphQL.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager


@Configuration
class SecurityConfig {

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user = User.withUsername("user")
            .password("{noop}password") // {noop} はプレーンテキストを意味します（開発用）
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf{
                it.disable()
            }
            .authorizeHttpRequests {
                // graphiqlは許可、それ以外は認証が必要
                it.requestMatchers("/graphiql").permitAll()
                it.requestMatchers("/api/graphql").authenticated()
                it.anyRequest().authenticated()
            }
            // GraphiQL 上でもログイン画面が表示されるように
            .httpBasic{}
        return http.build()
    }
}