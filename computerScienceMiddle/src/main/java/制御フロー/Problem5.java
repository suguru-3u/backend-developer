package 制御フロー;

/**
 * フィボナッチ数列
 *
 * <p>
 * Jack は魔法使いからもらった豆を裏庭に植えて昼寝をしました。
 * 昼寝から目覚めて裏庭を確認するとその豆は巨木へと成長し、雲の上にある巨人の城にたどりつくまでの大きさになっていました。
 * 豆を観察すると、以下の条件で 1 秒ずつ成長することがわかりました。
 * <p>
 * f(0) = 0
 * f(1) = 1
 * f(n) = f(n-1) + f(n-2) (n ≥ 2)
 * 整数 n が与えられるので、n 秒後の木の高さを求める、fibonacci という関数を作成してください。
 * <p>
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： integer
 * <p>
 * fibonacci(5) --> 5
 * fibonacci(9) --> 34
 * fibonacci(10) --> 55
 * fibonacci(19) --> 4181
 */

class Problem5 {
    public static int fibonacci(int n) {
        int f0 = 0;
        int f1 = 1;
        int current = 0;

        if (n == 0) return f0;
        if (n == 1) return f1;

        for (int i = 1; i <= n; i++) {
            current = f0 + f1;
            f0 = f1;
            f1 = current;
        }

        return current;
    }
}

