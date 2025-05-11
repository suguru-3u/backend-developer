package com.sample.oauth.controllers

import com.sample.oauth.model.InputUserRegisterInfo
import com.sample.oauth.model.JwtToken
import com.sample.oauth.security.CustomUserDetailsService
import com.sample.oauth.security.RefreshTokenService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController(
    private val customUserDetailsService: CustomUserDetailsService,
    private val refreshTokenService: RefreshTokenService
) {
    @PostMapping("/api/user/register")
    fun register(@RequestBody inputUserRegisterInfo: InputUserRegisterInfo): ResponseEntity<JwtToken.Token> {
        // ユーザを登録してJWTトークンを発行する
        return ResponseEntity.ok(customUserDetailsService.register(inputUserRegisterInfo))
    }

    @PostMapping("/api/refresh-token")
    fun refreshToken(request: HttpServletRequest): ResponseEntity<out Any> {
        try {
            return ResponseEntity.ok(refreshTokenService.execute(request))
        } catch (e: Exception) {
            println("Message: ${e.message}")
            return ResponseEntity.status(400).body("アクセストークンの作成に失敗しました")
        }
    }

    @GetMapping("/api/sample")
    fun sample(): ResponseEntity<String> {
        return ResponseEntity.ok().body("認証に成功してAPIリクエストに成功しました")
    }

    @PostMapping("/api/logout")
    fun logout() {

    }
}
