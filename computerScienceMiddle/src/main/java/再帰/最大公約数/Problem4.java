package 再帰.最大公約数;

/**
 * 既約分数
 *
 * <p>
 * 自然数
 * x,yによって構成される分数x/yの既約分数を文字列として返す、irreducibleFraction という関数を作成してください。
 * 既約分数とは、それ以上約分できない分数のことです。
 * <p>
 * 例えば、
 * 24/42が与えられたとき、分母分子共に6で割ることによって、既約分数である4/7を作ることができます。
 */

class Problem4 {
    public static String irreducibleFraction(int x, int y) {
        int gcd = gcd(x, y);
        x = x / gcd;
        y = y / gcd;
        return y == 1 ? "" + x : x + "/" + y;
    }

    public static int gcd(int x, int y) {
        if (y == 0) return x;
        return gcd(y, x % y);
    }
}

