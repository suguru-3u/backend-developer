package com.sample.oauth.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component


@Component
class CustomOAuth2SuccessHandler(
    private val jwtProvider: JwtProvider,
    private val redisProvider: RedisProvider
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userName = SecurityContextHolder.getContext().authentication.name
        val jwtToken =
            jwtProvider.generateJwtToken(userName)
        redisProvider.set(
            key = "refresh:${userName}:${jwtToken.token.refreshToken}",
            value = jwtToken.token.refreshToken,
            seconds = jwtToken.expiration.refreshTokenExpiration.time
        )

        // HttpServletResponseはテキストかJsonしかレスポンスしないため、Json化
        val responseJson = jacksonObjectMapper().writeValueAsString(jwtToken.token)

        response.status = HttpServletResponse.SC_OK
        response.contentType = "application/json"
        response.writer.write(responseJson)
    }
}