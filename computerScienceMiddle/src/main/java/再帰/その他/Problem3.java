package 再帰.その他;

/**
 * 3で割り続ける
 *
 * <p>
 * 3 の累乗 n が与えられるので、整数 n を 3 で除算できる回数を返す、divideBy3Count という関数を作成してください。
 * <p>
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： integer
 * <p>
 * divideBy3Count(1) --> 0
 * divideBy3Count(3) --> 1
 * divideBy3Count(27) --> 3
 * divideBy3Count(729) --> 6
 * divideBy3Count(6561) --> 8
 */

class Problem3 {
    public static int divideBy3Count(int n){
        if(n / 3 == n) return 0;
        return divideBy3Count(n % 3) + 1;
    }
}

