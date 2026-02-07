package 再帰.最大公約数;

/**
 * kで割り続ける
 *
 * <p>
 * 自然数 n, k が与えられるので、n が k で何回割り切れるか返す、countDivisibleByK という関数を作成してください。
 * 例えば、28 は 2 で割ると 14、14 を 2 で割ると 7 なので、2 で割った回数 2 を返します。ただし、k は 2 以上とします。
 * <p>
 * <p>
 * 入力のデータ型： integer n, integer k
 * 出力のデータ型： integer
 * <p>
 * countDivisibleByK(3,2) --> 0
 * countDivisibleByK(30,5) --> 1
 * countDivisibleByK(10,2) --> 1
 * countDivisibleByK(24,2) --> 3
 * countDivisibleByK(243,3) --> 5
 * countDivisibleByK(1024,2) --> 10
 * countDivisibleByK(1048576,2) --> 20
 */

class Problem1 {
    public static int countDivisibleByK(int n, int k) {
        // 関数を完成させてください
        if (n % k != 0) return 0;
        return countDivisibleByK(n / k, k) + 1;
    }
}

