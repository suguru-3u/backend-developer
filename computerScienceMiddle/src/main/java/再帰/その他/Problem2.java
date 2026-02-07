package 再帰.その他;

/**
 * 2の倍数の合計
 *
 * <p>
 * 自然数 n が与えられるので、2×1, 2×2, ..., 2×n までの総和の総和を返す、
 * multipleOfTwoTotal という関数を作成してください。
 * 例えば、n = 3 のとき、(2×1 + 2×2 + 2×3) + (2×1 + 2×2) + (2×1) = 20 が返されます。
 * <p>
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： integer
 * <p>
 * multipleOfTwoTotal(1) --> 2
 * multipleOfTwoTotal(2) --> 8
 * multipleOfTwoTotal(3) --> 20
 * multipleOfTwoTotal(4) --> 40
 * multipleOfTwoTotal(5) --> 70
 * multipleOfTwoTotal(6) --> 112
 * multipleOfTwoTotal(7) --> 168
 * multipleOfTwoTotal(8) --> 240
 * multipleOfTwoTotal(9) --> 330
 */

class Problem2 {
    public static int multipleOfTwoTotal(int n) {
        if (n <= 0) return 0;
        return multipleOfTwoTotalHelper(n) + multipleOfTwoTotal(n - 1);
    }

    public static int multipleOfTwoTotalHelper(int n) {
        if (n <= 0) return 0;
        return multipleOfTwoTotalHelper(n - 1) + n * 2;
    }
}

