package 抽象化;

/**
 * ピタゴラス数
 *
 * <p>
 * x-y グラフにおいて、点 A（x,y）が与えられるので、原点から点 A までの距離が自然数かどうかチェックする、
 * isPerfectSquare という関数を定義してください。
 * 自然数は、x*2 + y*2 した結果を平方根にして求めると回答できる
 * <p>
 *
 * 入力のデータ型： integer x, integer y
 * 出力のデータ型： bool
 *
 * isPerfectSquare(3,4) --> true
 * isPerfectSquare(5,12) --> true
 * isPerfectSquare(8,15) --> true
 * isPerfectSquare(7,24) --> true
 * isPerfectSquare(1,3) --> false
 */

class Problem3 {
    public static boolean isPerfectSquare(int x, int y){
        // 関数を完成させてください
        return hasDecimal(calculateSquare(x, y));
    }

    private static boolean hasDecimal(double number){
        return number % 1 == 0;
    }

    private static double calculateSquare(int x, int y){
        return  Math.sqrt(x * x + y * y);
    }
}

