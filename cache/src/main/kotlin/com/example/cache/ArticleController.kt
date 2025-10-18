package com.example.cache

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
class ArticleController {

    // データベースから記事データを取得するサービスを想定
    // 実際には外部サービスやDBからデータを取得します
    private fun getArticleData(id: Long): String {
        // 記事のコンテンツと、そのコンテンツのバージョン/ハッシュ値を返す
        return "Article Content for $id - Version X.Y.Z"
    }

    @GetMapping("/api/articles/{id}")
    fun getArticle(@PathVariable id: Long): ResponseEntity<String> {
        val content = getArticleData(id)

        // 1. ETagを生成 (弱いキャッシュ)
        // ここではコンテンツのシンプルなハッシュをETagとして使用
        val etagValue = "\"${content.hashCode()}\""

        // 2. Cache-Controlを設定 (強いキャッシュ)
        // max-age=60秒を設定し、1分間は検証なしでキャッシュを利用可能にする
        val cacheControl = CacheControl.maxAge(Duration.ofSeconds(60))
            .mustRevalidate() // max-age切れ後に再検証を強制 (Cache-Control: max-age=60, must-revalidate)

        return ResponseEntity.ok()
            .cacheControl(cacheControl) // Cache-Controlヘッダーを設定
            .eTag(etagValue)            // ETagヘッダーを設定
            .body(content)
    }
}