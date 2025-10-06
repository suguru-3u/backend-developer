package algorithm.ソート.バブルソート

/**
 * バブルソート
 * Bubble Sort
 * 手順1.	配列の先頭から隣り合う要素を比較し、前の要素が後ろの要素より大きい場合は交換する。
 * 手順2.	配列の末尾まで手順1を繰り返す。
 * 手順3.	手順1と手順2を配列の要素数-1回繰り返す。
 * 例）5,3,8,4,2
 * 1回目のループ：3,5,4,2,8
 * 2回目のループ：3,4,2,5,8
 * 3回目のループ：3,2,4,5,8
 * 4回目のループ：2,3,4,5,8
 */

fun main(){
    val targetArray = intArrayOf(5,3,8,4,2)

    for(i in targetArray.indices){
       for(j in 1..<targetArray.size - i){
           if(targetArray[j - 1] > targetArray[j]){
               val temp = targetArray[j - 1]
               targetArray[j - 1] = targetArray[j]
               targetArray[j] = temp
           }
       }
    }

    println(targetArray.joinToString(","))
}