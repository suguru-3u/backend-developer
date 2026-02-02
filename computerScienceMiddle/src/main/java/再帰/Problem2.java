package 再帰;

/**
 * 足し算（再帰）
 *
 * <p>
 * 0 以上の整数 m と n が与えられるので、m + n を返す、recursiveAddition という関数を再帰を用いて作成してください。
 * <p>
 *
 * 入力のデータ型： integer m, integer n
 * 出力のデータ型： integer
 *
 * recursiveAddition(5,3) --> 8
 * recursiveAddition(10,7) --> 17
 * recursiveAddition(21,30) --> 51
 */

class Problem2 {
    public static int recursiveAddition(int m, int n){
        if(n == 0) return m;
        return recursiveAddition(m, n - 1) + 1;
    }
}

