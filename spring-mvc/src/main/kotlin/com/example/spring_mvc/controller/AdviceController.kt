package com.example.spring_mvc.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

// グローバルな例外処理を行うためのクラス
@ControllerAdvice
class AdviceController {

    // キャッチする例外を指定
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<Map<String, String?>> {
        println("例外キャッチ")

        // ヘッダーを生成し、Content-Typeにapplication/jsonを設定
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500エラーを設定
            .body(mapOf("error" to ex.message)) // 例外メッセージをJSON形式で返す
    }
}