package 再帰.基本;

/**
 * 正方形の合計面積
 *
 * <p>
 * 1 辺 1 の正方形が 1 つあります。これを 1 列目として、2 列目に 1 辺が 2 の正方形を 2 つ追加します。
 * 同様に 1 辺の長さと正方形の個数を列ごとに増やしていくと、x 列目には 1 辺 x の正方形が x 個追加されることになります。
 * 自然数 x が与えられるので、1辺が 1 の正方形から 1 辺 x の正方形まで、すべての正方形の面積を足し合わせた合計値を返す
 * totalSquareArea いう関数を再帰によって作成してください。総和や 3 乗を計算するために必要な他の関数は用いて構いません。
 * <p>
 *
 * 入力のデータ型： integer x
 * 出力のデータ型： integer
 *
 * totalSquareArea(1) --> 1
 * totalSquareArea(2) --> 9
 * totalSquareArea(3) --> 36
 * totalSquareArea(4) --> 100
 * totalSquareArea(5) --> 225
 * totalSquareArea(12) --> 6084
 * totalSquareArea(10) --> 3025
 */

class Problem11 {
    public static int totalSquareArea(int x){
        if(x == 1) return 1;
        return totalSquareArea(x - 1) + ((x * x) * x);
    }
}

