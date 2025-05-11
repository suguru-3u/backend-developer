package com.sample.oauth.security

import com.sample.oauth.config.RedisConfig
import com.sample.oauth.model.InputUserRegisterInfo
import com.sample.oauth.model.JwtToken
import com.sample.oauth.model.UserRole
import com.sample.oauth.repository.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val redisProvider: RedisProvider,
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw Exception("User not found")

        return User(
            user.email,
            user.password,
            listOf(SimpleGrantedAuthority(user.role.name))
        )
    }

    fun register(inputUserRegisterInfo: InputUserRegisterInfo): JwtToken.Token {
        val encodePassword = BCryptPasswordEncoder().encode(inputUserRegisterInfo.password)

        val user = userRepository.findByEmail(inputUserRegisterInfo.email)
        if (user != null) {
            throw Exception("すでに登録済みです")
        }

        userRepository.register(inputUserRegisterInfo.email, encodePassword)

        val registerUser = User(
            inputUserRegisterInfo.email,
            encodePassword,
            listOf(
                SimpleGrantedAuthority(UserRole.USER.name)
            )
        )
        val authentication = UsernamePasswordAuthenticationToken(
            registerUser,
            registerUser.password,
            registerUser.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication

        // アクセストークン・リフレッシュトークンの作成＆Redisにリフレッシュトークンの作成
        val jwtToken = jwtProvider.generateJwtToken(registerUser.username)
        redisProvider.set(
            key = "refresh:${SecurityContextHolder.getContext().authentication.name}:${jwtToken.token.refreshToken}",
            value = jwtToken.token.refreshToken,
            seconds = jwtToken.expiration.refreshTokenExpiration.time
        )

        return jwtToken.token
    }
}
