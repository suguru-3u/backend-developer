package 再帰.基本;

/**
 * パスカルの三角形
 *
 * <p>
 * 図のように三角数の数列があります。天才 Pascal は小学生の時にこの並びを見て規則的な発見をしました。
 * 自然数 x が与えられるので、x 番目の三角形に含まれるドットの数を返す、numberOfDots という関数を再帰を使って作成してください。
 * <p>
 *
 * 入力のデータ型： integer x
 * 出力のデータ型： integer
 *
 * numberOfDots(2) --> 3
 * numberOfDots(4) --> 10
 * numberOfDots(5) --> 15
 * numberOfDots(10) --> 55
 */

class Problem10 {
    public static int numberOfDots(int x){
        if(x == 1) return 1;
        return  numberOfDots(x - 1) + x;
    }
}

