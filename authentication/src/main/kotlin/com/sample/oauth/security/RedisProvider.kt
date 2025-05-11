package com.sample.oauth.security

import com.sample.oauth.config.RedisConfig
import org.springframework.stereotype.Component
import redis.clients.jedis.params.ScanParams
import redis.clients.jedis.resps.ScanResult

@Component
class RedisProvider(
    private val redisConfig: RedisConfig
) {
    fun set(key: String, value: String, seconds: Long) {
        redisConfig.jedis.resource.use {
            it.setex(key, seconds, value)
        }
    }

    fun existBlackAccessToken(key: String): Boolean {
        return exist("blacklist:accessToken:${key}")
    }

    fun notExistRefreshToken(key: String, userName: String): Boolean {
        return !exist("refresh:$userName:$key")
    }

    private fun exist(key: String): Boolean {
        val result = redisConfig.jedis.resource.use {
            it.get(key)
        }
        return result != null
    }

    fun delete(key: String) {
        redisConfig.jedis.resource.use { jedis ->
            var cursor: String = ScanParams.SCAN_POINTER_START
            val scanParams = ScanParams().match(key).count(100) // 一回に最大100件取る設定

            do {
                val scanResult = jedis.scan(cursor, scanParams)
                val keys = scanResult.result

                if (keys.isNotEmpty()) {
                    jedis.del(*keys.toTypedArray())
                }

                cursor = scanResult.cursor
            } while (cursor != ScanParams.SCAN_POINTER_START)
        }
    }
}