package algorithm.ユークリッドの互除法

import java.util.*


/**
 * ユークリッドの互除法を用いて最大公約数を求める
 * huta
 *
 * 手順1.	変数dにmとnの割り算の余りを代入する。
 * 手順2.	dが0なら、nを最大公約数として出力する。
 * 手順3.	mにnの値を代入する。
 * 手順4.	nにdの値を代入する。
 * 手順5.	手順1に戻る。
 */

// 1つの数値の最大公約数を求める
fun main() {
    val scanner = Scanner(System.`in`)

    val n = scanner.nextInt()
    val a = IntArray(n)
    for (i in 0 until n) {
        a[i] = scanner.nextInt()
    }

    var maxCount = 0
    var answer = 2
    for (k in 2..1000) {
        var count = 0
        for (i in 0 until n) {
            if (a[i] % k == 0) {
                count++
            }
        }
        if (count > maxCount) {
            maxCount = count
            answer = k
        }
    }
    println(answer)

    scanner.close()
}

// 2つの数値の最大公約数を求める
fun main1() {
    val scanner = Scanner(System.`in`)
    var input1 = scanner.nextInt()
    var input2 = scanner.nextInt()

    if (input1 < input2) {
        val temp = input1
        input1 = input2
        input2 = temp
    }

    val flg = true
    while (flg) {
        val result1 = input1 % input2
        if (result1 == 0) {
            println("最大公約数は $input2 です")
            break
        } else {
            input1 = input2
            input2 = result1
        }
    }
}

// 複数の数値の最大公約数を求める
fun main2() {
    val scanner = Scanner(System.`in`)

    val count = scanner.nextInt()
    val numbers = IntArray(count)
    for (i in 0 until count) {
        numbers[i] = scanner.nextInt()
    }

    var result1 = 0
    var input1: Int
    var input2: Int
    for (i in 0 until count) {
        var isNotSkip = true
        if (result1 == 0) {
            input1 = numbers[i]
            input2 = numbers[i + 1]
        } else {
            input1 = result1
            input2 = numbers[i]
        }

        if (input1 < input2) {
            val temp = input1
            input1 = input2
            input2 = temp
        }

        while (isNotSkip) {
            val result2 = input1 % input2
            if (result2 == 0) {
                result1 = input2
                isNotSkip = false
            } else if (result2 == 1) {
                isNotSkip = false
            } else {
                input1 = input2
                input2 = result2
            }
        }
    }
    println(result1)

    scanner.close()
}