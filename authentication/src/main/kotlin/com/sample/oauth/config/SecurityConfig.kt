package com.sample.oauth.config

import com.sample.oauth.security.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig(
    // 認証されていない状態でアクセスしようとした際に表示するメッセージを管理しているクラス
    private val customFailedAuthenticationEntry: CustomFailedAuthenticationEntry,
    // Oauth認証成功時の処理を定義したクラス
    private val oAuth2SuccessHandler: CustomOAuth2SuccessHandler,
    private val jwtProvider: JwtProvider,
    private val redisProvider: RedisProvider
) {
    // パスワードをエンコード・デコードするための関数
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun customAuthenticationFilter(authenticationManager: AuthenticationManager): CustomLoginAuthenticationFilter {
        val filter = CustomLoginAuthenticationFilter(jwtProvider, redisProvider)
        filter.setAuthenticationManager(authenticationManager)
        return filter
    }

    @Bean
    fun customApiAuthenticationFilter(): CustomApiAuthenticationFilter {
        return CustomApiAuthenticationFilter(jwtProvider, redisProvider)
    }

    // 認証周りの設定関数
    @Bean
    fun filterChain(
        http: HttpSecurity,
        customLoginAuthenticationFilter: CustomLoginAuthenticationFilter,
        customApiAuthenticationFilter: CustomApiAuthenticationFilter
    ): SecurityFilterChain {
        // CSRF対策はAPIなので一旦無効
        http.csrf { it.disable() }
            // フォームログインは無効
            .formLogin { it.disable() }
            // APIベースの認証のためセッション保持はしない
            .sessionManagement {
                it
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // リクエスト許可するURL設定
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                    .anyRequest().authenticated()
            }
            // Oauth関係の設定
            .oauth2Login {
                it
                    .successHandler(oAuth2SuccessHandler) // OAuth認証成功後の処理
            }
            .logout {
                it.logoutUrl("/api/logout")
                    .logoutSuccessHandler(CustomLogoutSuccessHandler(jwtProvider, redisProvider))
            }
            // 認証に失敗した時の設定
            .exceptionHandling {
                it.authenticationEntryPoint(customFailedAuthenticationEntry)
            }
            .addFilterBefore(
                customLoginAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterBefore(
                customApiAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
        return http.build()
    }
}
