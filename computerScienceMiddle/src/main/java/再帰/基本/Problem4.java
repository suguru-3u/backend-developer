package 再帰.基本;

/**
 * 総和
 *
 * <p>
 * 1 以上の整数 n が与えられるので、1 + 2 + 3 + ... + n を計算する
 * simpleSummation という関数を再帰を使って作成してください。
 * <p>
 *
 * 入力のデータ型： integer n
 * 出力のデータ型： integer
 *
 * simpleSummation(3) --> 6
 * simpleSummation(10) --> 55
 * simpleSummation(100) --> 5050
 * simpleSummation(54) --> 1485
 */

class Problem4 {
    public static int simpleSummation(int n){
        if(n == 0) return 0;
        return simpleSummation(n - 1) + n;
    }
}

