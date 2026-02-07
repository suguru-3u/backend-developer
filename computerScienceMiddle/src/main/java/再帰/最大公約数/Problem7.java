package 再帰.最大公約数;

import java.util.Objects;

/**
 * 正方形の合計枚数
 *
 * <p>
 * Thomas は図画工作で色紙を使って飛行機を作成しています。
 * 色紙にはさまざまなサイズが用意されており、選択することができます。
 * 今、Thomas は長方形の色紙から整数値を 1 辺とする、
 * できるだけ大きく、かつ同じ大きさの正方形を何枚も切り取ることを計画しています。
 *
 * 長方形の大きさとして、縦 x、横 y が与えられるので、正方形の合計枚数を返す、
 * countSquare という関数を作成してください。ただし、入力 x , y はどちらも整数とします。
 * <p>
 *
 * 入力のデータ型： integer x, integer y
 * 出力のデータ型： integer
 *
 * countSquare(28,32) --> 56
 * countSquare(20,32) --> 40
 * countSquare(8177,3315) --> 555
 */

class Problem7 {
    public static int countSquare(int x, int y) {
        int gcd = gcd(x,y);
        return x / gcd * y / gcd;
    }

    public static int gcd(int x, int y) {
        if (y == 0) return x;
        return gcd(y, x % y);
    }
}

