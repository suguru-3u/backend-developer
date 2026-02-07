package 再帰.最大公約数;

/**
 * 3つの最大公約数
 *
 * <p>
 * 自然数 x, y, z が与えられるので、x, y, z の最大公約数を返す、threeGCD という関数を作成してください。
 * <p>
 * <p>
 * 入力のデータ型： integer x, integer y, integer z
 * 出力のデータ型： integer
 * <p>
 * threeGCD(12,18,24) --> 6
 * threeGCD(30,243,91) --> 1
 * threeGCD(1029,1071,2100) --> 21
 * threeGCD(3180,1908,72) --> 12
 */

class Problem3 {
    public static int threeGCD(int x, int y, int z) {
        return twoGCD(twoGCD(x, y), z);
    }

    public static int twoGCD(int x, int y) {
        if (y == 0) return x;
        return twoGCD(y, x % y);
    }
}

