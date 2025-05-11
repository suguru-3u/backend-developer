package com.sample.oauth.config

import org.springframework.stereotype.Component
import redis.clients.jedis.JedisPool

@Component
class RedisConfig{
    private val redisHost = "localhost"

    private val redisPort = 6379

    val jedis = JedisPool(redisHost, redisPort)



}