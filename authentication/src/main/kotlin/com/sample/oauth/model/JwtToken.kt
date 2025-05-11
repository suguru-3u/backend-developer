package com.sample.oauth.model

import java.util.*

data class JwtToken(
    val token: Token,
    val expiration: Expiration,
) {
    data class Token(
        val accessToken: String,
        val refreshToken: String,
    )

    data class Expiration(
        val accessTokenExpiration: Date,
        val refreshTokenExpiration: Date,
    )
}
