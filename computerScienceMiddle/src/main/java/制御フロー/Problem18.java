package 制御フロー;

/**
 * 素数の和
 *
 * <p>
 * ある国は長く存続できたことに感謝を込め、国民に給付金を渡そうと考えました。
 * そこで、建国から経過した年数 n までに含まれている、全ての素数を足した数を給付金にする予定です。
 * 自然数 n が与えられるので、給付金の額を返す sumOfAllPrimes という関数を作成してください。
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： integer
 * <p>
 * sumOfAllPrimes(1) --> 0
 * sumOfAllPrimes(2) --> 2
 * sumOfAllPrimes(3) --> 5
 * sumOfAllPrimes(100) --> 1060
 * sumOfAllPrimes(1000) --> 76127
 */

class Problem18 {
    public static int sumOfAllPrimes(int n) {
        int sumPrime = 0;

        for (int i = 2; i <= n; i++) {
            for (int j = 2; j <= i; j++) {
                if (i % j == 0 && i != j) break;
                if (i == j) sumPrime += i;
            }
        }

        return sumPrime;
    }

    public static int sumOfAllPrimesV2(int n) {
        int sumPrime = 0;

        for (int i = 2; i <= n; i++) {
            if (isPrime(i)) {
                sumPrime += i;
            }
        }

        return sumPrime;
    }

    private static boolean isPrime(int num) {
        if (num <= 1) return false;
        if (num <= 3) return true;
        if (num % 2 == 0 || num % 3 == 0) return false;

        for (int i = 5; i * i <= num; i += 6) {
            if (num % i == 0 || num % (i + 2) == 0) return false;
        }

        return true;
    }
}

