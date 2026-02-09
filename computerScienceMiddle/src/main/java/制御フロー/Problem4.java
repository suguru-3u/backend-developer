package 制御フロー;

/**
 * 二乗の総和
 *
 * <p>
 * 自然数 n が与えられるので、12 + 22 + 32 + ... + n2 を計算する
 * squareSummation という関数を再帰を使って作成してください。
 * <p>
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： integer
 * <p>
 * squareSummation(2) --> 5
 * squareSummation(3) --> 14
 * squareSummation(4) --> 30
 * squareSummation(10) --> 385
 * squareSummation(15) --> 1240
 * squareSummation(100) --> 338350
 * squareSummation(208) --> 3021304
 */

class Problem4 {
    public static int squareSummation(int n){
        int total = 0;
        for(int i = n; 0 < i; i--){
            total += (int) Math.pow(i, 2);
        }
        return total;
    }
}

