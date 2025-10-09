package algorithm.ソート.クイックソート

/**
 * クイックソート
 * Quick Sort
 * 手順1.配列の中から基準値（ピボット）を1つ選ぶ。
 * （ピボットの選び方にはいくつかありますが、一般的には先頭、末尾、中央、またはランダムな要素などが選ばれる）
 * 手順2.基準値より小さい値を左側に、大きい値を右側に集める。
 * 手順3.左側と右側の配列に対して、手順1と手順2を繰り返す。
 * 例）5,3,8,4,2
 * 1回目のループ：4,3,2,5,8
 * 2回目のループ：2,3,4,5,8
 */

fun main() {
    // サンプルデータ
    val data = listOf(10, 7, 8, 9, 1, 5)

    // ソート実行
    val sortedData = quickSort(data)

    // 結果出力
    println("元のデータ: $data")
    println("ソート後のデータ: $sortedData")

    // サンプルデータ (MutableListを使用)
    val data2 = mutableListOf(10, 7, 8, 9, 1, 5, 3)

    println("ソート前のデータ: $data2")

    // quicksortInPlaceの初期呼び出し: リスト全体の範囲 (0から末尾) を指定
    quicksortInPlace(data2, 0, data.size - 1)

    println("ソート後のデータ: $data2")
}

fun quickSort(arr: List<Int>): List<Int> {
    // 1. ベースケース: 要素数が1以下のリストはすでにソート済み
    if (arr.size <= 1) {
        return arr
    }

    // 2. ピボットの選択: ここではリストの最初の要素をピボットとする
    val pivot = arr[0]

    // 3. 分割 (パーティション):
    // filter関数を使用して、リストを3つのグループに分割
    val left = arr.filter { it < pivot }     // ピボットより小さい要素
    val equal = arr.filter { it == pivot }   // ピボットと等しい要素
    val right = arr.filter { it > pivot }    // ピボットより大きい要素

    // 4. 再帰的なソートと結合
    // leftをソート + equal (ピボット) + rightをソート
    // flatMapでリストを結合する代わりに、ここではシンプルなリストの加算（Kotlinではリストの結合）を使用
    return quickSort(left) + equal + quickSort(right)
}

fun quicksortInPlace(arr: MutableList<Int>, low: Int, high: Int) {
    // ベースケース: ソート対象の範囲が1要素以下になったら終了
    if (low < high) {
        // 1. パーティション（分割）を実行し、ピボットの確定位置を取得
        val pivotIndex = partition(arr, low, high)

        // 2. ピボットの左側を再帰的にソート
        quicksortInPlace(arr, low, pivotIndex - 1)

        // 3. ピボットの右側を再帰的にソート
        quicksortInPlace(arr, pivotIndex + 1, high)
    }
}

fun partition(arr: MutableList<Int>, low: Int, high: Int): Int { // 10, 7, 8, 9, 1, 5, 3
    // ピボットとして範囲の最後の要素を選択
    val pivot = arr[high]
    var i = low - 1 // i は、ピボットより小さい要素の「最後のインデックス」を追跡

    // lowからhigh-1までの要素をスキャン
    for (j in low until high) {
        // 現在の要素がピボットより小さい場合
        if (arr[j] <= pivot) {
            i++
            // arr[j]（ピボットより小さい要素）をarr[i]の位置にスワップ
            // これにより、小さい要素が配列の左端に集められる
            arr.swap(i, j)
        }
    }

    // 最後に、ピボット（arr[high]）を、小さい要素グループのすぐ隣（i+1）に移動
    arr.swap(i + 1, high)

    // ピボットが確定した最終位置を返す
    return i + 1
}

// 拡張関数として、リスト内の要素を交換（スワップ）するヘルパー関数
fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}