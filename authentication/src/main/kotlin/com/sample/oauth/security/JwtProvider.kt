package com.sample.oauth.security

import com.sample.oauth.model.JwtToken
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtProvider {

    // 固定文字列をキーにする（本番ではもっと長いランダムな文字列を使うべき）
    private val secret = "your-very-secret-key-string-must-be-long-enough"

    private val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())  // 本番では設定ファイルから読むべき！

    fun generateJwtToken(tokenValue: String): JwtToken {
        val accessToken = generateAccessToken(tokenValue)
        val refreshToken = generateRefreshToken(tokenValue)

        val accessTokenExpiration = getExpirationDateFromToken(accessToken)
        val refreshTokenExpiration = getExpirationDateFromToken(refreshToken)

        return JwtToken(
            token = JwtToken.Token(
                accessToken = accessToken,
                refreshToken = refreshToken,
            ),
            expiration = JwtToken.Expiration(
                accessTokenExpiration = accessTokenExpiration,
                refreshTokenExpiration = refreshTokenExpiration,
            ),
        )
    }

    private fun generateAccessToken(email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + 1000 * 60 * 60 * 1) // 1時間後に有効期限切れ

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    private fun generateRefreshToken(email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + 1000 * 60 * 60 * 24) // 24時間後に有効期限切れ

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun getUserFromAuthenticationToken(token: String): String? {
        // トークンを解析してユーザーを取得
        if (!validateToken(token)) return null
        // JWTトークンからユーザー情報を取得
        return getUserInfoFromToken(token)
    }

    private fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            println(e.message)
            false
        }
    }

    private fun getUserInfoFromToken(token: String): String? {
        val jws = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
        return jws.body.subject
    }

    fun getExpirationDateFromToken(token: String): Date {
        val jws = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
        return jws.body.expiration
    }

    fun checkTokenExpirationBeforeNow(token: String): Boolean {
        val now = Date()
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(now)
    }
}
