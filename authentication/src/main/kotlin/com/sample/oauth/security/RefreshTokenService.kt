package com.sample.oauth.security

import com.sample.oauth.model.JwtToken
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val jwtProvider: JwtProvider,
    private val redisProvider: RedisProvider
) {
    fun execute(request: HttpServletRequest): JwtToken.Token {
        val token = CustomHttpServletRequest(request).getAuthenticationTokenFromRequest()
        val userName = jwtProvider.getUserFromAuthenticationToken(token)
            ?: throw Exception("リフレッシュトークンからユーザ情報が取得できません")
        if (redisProvider.notExistRefreshToken(
                token,
                userName
            )
        ) throw Exception("リフレッシュトークンが有効ではありません")
        val user = jwtProvider.getUserFromAuthenticationToken(token)
            ?: throw Exception("ユーザーの取得に失敗しました")
        return jwtProvider.generateJwtToken(user).token

    }
}