package 再帰.基本;

/**
 * 階乗
 *
 * <p>
 * 自然数 n が与えられるので、1 × 2 × 3 × ... × n を計算する
 * factorial という関数を再帰を使って作成してください。
 * <p>
 *
 * 入力のデータ型： integer n
 * 出力のデータ型： long
 *
 * factorial(2) --> 2
 * factorial(3) --> 6
 * factorial(5) --> 120
 * factorial(10) --> 3628800
 * factorial(15) --> 1307674368000
 */

class Problem5 {
    public static long factorial(int n){
        if(n == 0) return 1;
        return factorial(n - 1) * n;
    }
}

