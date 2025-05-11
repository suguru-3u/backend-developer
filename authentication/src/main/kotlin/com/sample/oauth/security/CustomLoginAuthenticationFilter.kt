package com.sample.oauth.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sample.oauth.model.InputUserAuthenticationInfo
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class CustomLoginAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val redisProvider: RedisProvider
) : UsernamePasswordAuthenticationFilter() {

    // ログインエンドポイントのカスタマイズ（ここで記述することでログインのルーティング・処理を設定できる）
    init {
        setFilterProcessesUrl("/api/login")
    }

    // Login認証処理
    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        if (request.method != "POST") {
            throw Exception("Only POST is allowed on /api/login")
        }

        // Jsonのデータ取得
        val requestBody = request.reader.readText()
        val loginRequest = ObjectMapper().readValue(
            requestBody,
            InputUserAuthenticationInfo::class.java
        )

        // 認証用データ作成
        val authRequest =
            UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)

        // 認証処理実行
        return authenticationManager.authenticate(authRequest)
    }

    // Loginが成功した後に呼ばれるメソッド
    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain, authResult: Authentication
    ) {
        // 認証結果をセキュリティコンテキストに保存
        SecurityContextHolder.getContext().authentication = authResult

        val jwtToken =
            jwtProvider.generateJwtToken(SecurityContextHolder.getContext().authentication.name)
        redisProvider.set(
            key = "refresh:${SecurityContextHolder.getContext().authentication.name}:${jwtToken.token.refreshToken}",
            value = jwtToken.token.refreshToken,
            seconds = jwtToken.expiration.refreshTokenExpiration.time
        )

        // HttpServletResponseはテキストかJsonしかレスポンスしないため、Json化
        val responseJson = jacksonObjectMapper().writeValueAsString(jwtToken.token)

        response.status = HttpServletResponse.SC_OK
        response.contentType = "application/json"
        response.writer.write(responseJson)
    }

    // Loginが失敗した後に呼ばれるメソッド
    override fun unsuccessfulAuthentication(
        request: HttpServletRequest, response: HttpServletResponse,
        failed: AuthenticationException
    ) {
        logger.warn("failed : ${failed.message}")
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("{\"error\": ログインに失敗しました}")
    }
}
