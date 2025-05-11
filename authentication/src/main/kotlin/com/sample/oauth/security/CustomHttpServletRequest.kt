package com.sample.oauth.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders

class CustomHttpServletRequest(
    private val request: HttpServletRequest,
) {
    // ヘッダー情報の取り出し＆存在確認＆存在する場合、トークンのレスポンス
    fun getAuthenticationTokenFromRequest(): String {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw Exception("AuthenticationTokenが設定されていません")
        }
        return authHeader.substring(7)
    }
}