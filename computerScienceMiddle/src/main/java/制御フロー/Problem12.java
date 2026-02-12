package 制御フロー;

/**
 * 素数
 *
 * <p>
 * Kate は音楽の野外フェスを行うことになり、入場者の中から抽選でプレゼントを渡す企画を立てています。
 * そこで、素数の値で入場した方を当選者とすることにしました。
 * 入場者番号 number が与えられるので、素数かどうか判定する isPrime という関数を作成してください。
 * <p>
 * 入力のデータ型： integer number
 * 出力のデータ型： bool
 * <p>
 * isPrime(1) --> false
 * isPrime(2) --> true
 * isPrime(3) --> true
 * isPrime(4) --> false
 * isPrime(25) --> false
 * isPrime(29) --> true
 * isPrime(36) --> false
 * isPrime(45) --> false
 * isPrime(85) --> false
 * isPrime(455) --> false
 */

class Problem12 {
    public static boolean isPrime(int number) {
        if (number < 2) return false;
        for (int i = 2; i * i <= number; i++) {
            if (number % i == 0) return false;
        }
        return true;
    }
}

