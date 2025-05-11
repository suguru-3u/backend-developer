package com.sample.oauth.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

class CustomLogoutSuccessHandler(
    private val jwtProvider: JwtProvider,
    private val redisProvider: RedisProvider
) : LogoutSuccessHandler {
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        val token = CustomHttpServletRequest(request).getAuthenticationTokenFromRequest()
        val tokenExpiration = jwtProvider.getExpirationDateFromToken(token)
        val userName = jwtProvider.getUserFromAuthenticationToken(token)
        if (redisProvider.existBlackAccessToken(token)) throw Exception("既にログアウト済みです")
        redisProvider.delete("refresh:$userName:*")
        redisProvider.set(
            key = "blacklist:accessToken:${token}",
            value = token,
            seconds = tokenExpiration.time
        )

        response.status = HttpServletResponse.SC_NO_CONTENT
    }
}