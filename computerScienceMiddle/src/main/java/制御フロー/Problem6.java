package 制御フロー;

/**
 * 総和の総和
 *
 * <p>
 * 自然数 n が与えられるので、1 から n までの総和の総和を返す、summationOfSummation という関数を作成してください。
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： integer
 * <p>
 * summationOfSummation(1) --> 1
 * summationOfSummation(2) --> 4
 * summationOfSummation(3) --> 10
 * summationOfSummation(4) --> 20
 * summationOfSummation(10) --> 220
 */

class Problem6 {
    public static int summationOfSummation(int n) {
        int total = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= i; j++) {
                total += j;
            }
        }

        return total;
    }

    // 数学の公式を使うと2重ループを回避することができる
    public static int summationOfSummationV2(int n) {
        int total = 0;
        for (int i = 1; i <= n; i++) {
            total += i * (i + 1) / 2;
        }
        return total;
    }
}

