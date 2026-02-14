package 制御フロー;

/**
 * 完全数
 *
 * <p>
 * 与えられた自然数 n に対して、2 から n までの範囲内に存在する全ての完全数を
 * '-' で区切って並べた文字列を返す関数 perfectNumberList を定義してください。
 * ここで完全数とは、自分自身以外の約数を全て足した値が自分自身に等しい自然数のことです。
 * ただし、完全数が存在しない場合は、文字列 'none' を返してください。
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： string
 * <p>
 * perfectNumberList(3) --> none
 * perfectNumberList(6) --> 6
 * perfectNumberList(28) --> 6-28
 * perfectNumberList(100) --> 6-28
 * perfectNumberList(496) --> 6-28-496
 * perfectNumberList(1000) --> 6-28-496
 * perfectNumberList(10000) --> 6-28-496-8128
 */

class Problem16 {
    public static String perfectNumberList(int n) {
        StringBuilder perfectNumberStr = new StringBuilder();

        for (int i = 1; i <= n; i++) {
            int totalNum = 0;
            for (int j = 1; j * j <= i; j++) {
                if (i % j == 0) {
                    totalNum += j;
                    if (j * j != i) {
                        totalNum += (i / j);
                    }
                }
            }
            System.out.println(i);
            System.out.println(totalNum);
            if (totalNum == i) {
                perfectNumberStr.append(i);
                perfectNumberStr.append("-");
            }
        }

        return perfectNumberStr.toString().isEmpty() ? "none" : perfectNumberStr.substring(0, perfectNumberStr.length() - 1);
    }

    // これでもいける
    public static String perfectNumberListV2(int n) {
        StringBuilder perfectNumberStr = new StringBuilder();

        for (int i = 2; i <= n; i++) {
            if (isPerfectNumber(i)) {
                perfectNumberStr.append(i).append("-");
            }
        }

        return perfectNumberStr.length() == 0 ? "none" : perfectNumberStr.substring(0, perfectNumberStr.length() - 1);
    }

    private static boolean isPerfectNumber(int num) {
        int totalNum = 0;
        for (int j = 1; j <= Math.sqrt(num); j++) {
            if (num % j == 0) {
                totalNum += j;
                if (j != 1 && j != num / j) {
                    totalNum += num / j;
                }
            }
        }
        return totalNum == num;
    }
}

