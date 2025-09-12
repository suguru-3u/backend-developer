package com.example.spring_aop.sample

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component

/**
 * AOP（アスペクト指向プログラミング）
 * AOPの仕組みは、トランザクション・認証・キャッシュ・非同期・リトライ処理など
 * 様々な箇所で使用されている
 */

@Aspect
@Component
class AOPSample {
    // executionについて、戻り値、パッケージ名、クラス名・メソッド名、引数の順番で記述する
    // execution意外にもwithinとbeanを用いても設定することができる
    @Before("execution(* *..*Impl.*(..))")
    fun startLog(jp: JoinPoint) {
        println("メソッド開始: " + jp.signature);
        val targetObject = jp.target
        val thisObject = jp.`this`
        println("オブジェクト： $targetObject")
        println("オブジェクト： $thisObject")
    }

    @After("execution(* *..*Impl.*(..))")
    fun endLog(jp: JoinPoint) {
        println("メソッド終了: " + jp.signature);
    }
}