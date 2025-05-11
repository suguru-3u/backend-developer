package com.sample.oauth.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomFailedAuthenticationEntry(
    private val logger: Logger = org.slf4j.LoggerFactory.getLogger(
        CustomFailedAuthenticationEntry::class.java
    )
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.warn("failed : ${authException.message}")
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.characterEncoding = "UTF-8"
        response.writer.write("""{"error": "認証に失敗しました。"}""")
    }
}