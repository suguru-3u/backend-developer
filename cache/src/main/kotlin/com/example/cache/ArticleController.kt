package com.example.cache

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import java.time.Duration

@RestController
class ArticleController {

    // 💡 ステップ1: DBアクセスを最適化するため、データとバージョンを保持するクラス
    data class Article(val id: Long, val content: String, val versionHash: String)

    // 💡 ステップ2: データベースから「データとバージョン」を取得する想定の関数
    private fun getArticleDataWithVersion(id: Long): Article {
        // 実際には、DBの特定のカラム（例：updated_atのタイムスタンプやバージョンID）を元に
        // versionHashを生成します。この処理はコンテンツ全体を取得するよりも軽量であるべきです。
        val content = "Article Content for $id - Version X.Y.Z"
        val versionHash = content.hashCode().toString() // シンプルな例として使用
        return Article(id, content, "\"$versionHash\"")
    }

    @GetMapping("/api/articles/{id}")
    fun getPublicResource(@PathVariable id: Long, request: WebRequest): ResponseEntity<String> {
        print("Fetching article with ID: $id\n")
        // 1. データ本体ではなくバージョン情報（ETag）と、そのデータ本体を取得
        val article = getArticleDataWithVersion(id)
        val etagValue = article.versionHash

        // 2. Springの機能でクライアントのETagと比較し、一致するか検証
        if (request.checkNotModified(etagValue)) {
            // ETagが一致した場合 (304 Not Modified)
            // この時点でDBからコンテンツ本体を取得する処理（article.content）はスキップされ、
            // サーバー負荷が大幅に軽減されます。
            return ResponseEntity.status(304).eTag(etagValue).build()
        }

        val cacheControl = CacheControl.maxAge(Duration.ofSeconds(60))
            .mustRevalidate() // max-age切れ後に再検証を強制 (Cache-Control: max-age=60, must-revalidate)

        // 💡 修正: ハードコードされた文字列ではなく、DBから取得したコンテンツを使用
        val content = article.content


        return ResponseEntity.ok()
            .cacheControl(cacheControl) // Cache-Controlヘッダーを設定
            .eTag(etagValue)            // ETagヘッダーを設定
            .body(content)
    }
}