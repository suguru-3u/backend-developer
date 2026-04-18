package com.example.spring_load_test.service

import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SampleService {

    fun sample(): Long {
        val startTime = System.currentTimeMillis() // 処理開始時間を取得
        val runtime = Runtime.getRuntime()
        val beforeMem = runtime.totalMemory() - runtime.freeMemory()

        val users = mutableListOf<User>();

        // 1万件のユーザー作成：1 ~ 2ms メモリ使用量 0MB以下
        // 10万件のユーザー作成：5 ~ 12ms メモリ使用量 6MB前後
        // 100万件のユーザー作成：40 ~ 120ms メモリ使用量 70MB前後
        // 1000万件のユーザー作成：500ms 前後 メモリ使用量 600MB前後
        for (i in 1..10000) {
            users.add(User(i));
        }

        println("usersのサイズ ${users.size}")

        val endTime = System.currentTimeMillis() // 処理終了時間を取得
        val elapsedTime = endTime - startTime // 処理時間を計算

        println("処理時間: $elapsedTime ms")

        val afterMem = runtime.totalMemory() - runtime.freeMemory()
        println("メモリ使用量: ${(afterMem - beforeMem) / 1024 / 1024} MB")
        println("最大ヒープ: ${runtime.maxMemory() / 1024 / 1024} MB")

        return elapsedTime
    }

    data class User(
        val id: Int,
        val name: String = "name",
        val age: Int = 20,
        val email: String = "email",
        val address: String = "address",
        val phone: String = "phone",
        val website: String = "website",
        val sex: String = "sex",
        val sample1: String = "sample1",
        val sample2: String = "sample2",
    )
}

//measureTimeMillis