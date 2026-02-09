package 制御フロー;

/**
 * 階乗
 *
 * <p>
 * 自然数 n が与えられるので、1 × 2 × 3 × ... × n を計算する factorial という
 * 関数を再帰を使って作成してください。
 * <p>
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： long
 * <p>
 * factorial(2) --> 2
 * factorial(3) --> 6
 * factorial(5) --> 120
 * factorial(10) --> 3628800
 * factorial(15) --> 1307674368000
 */

class Problem3 {
    public static long factorial(int n) {
        long total = 1;
        for (int i = n; 0 < i; i--) {
            total *= i;
        }
        return total;
    }
}

