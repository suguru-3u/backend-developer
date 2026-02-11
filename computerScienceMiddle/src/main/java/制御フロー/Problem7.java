package 制御フロー;

/**
 * 整数カウント
 *
 * <p>
 * 0 から 9 までの整数 digit、自然数 idStart、idEnd が与えられるので、idStart 以上 idEnd 以下の中に digit が出現する回数を返す、
 * countBetweenNumbers という関数を作成してください。例えば、1, 10, 13 の場合、10、11、12、13 の中に 1 は 5 つ含まれます。
 * <p>
 * 入力のデータ型： integer digit, integer idStart, integer idEnd
 * 出力のデータ型： integer
 * <p>
 * countBetweenNumbers(1,1,20) --> 12
 * countBetweenNumbers(5,1,100) --> 20
 * countBetweenNumbers(3,100,250) --> 35
 * countBetweenNumbers(3,1,1000) --> 300
 * countBetweenNumbers(1,1,300) --> 160
 * countBetweenNumbers(9,899,1000) --> 122
 */

class Problem7 {
    public static int countBetweenNumbers(int digit, int idStart, int idEnd) {
        char target = Integer.toString(digit).charAt(0);
        int count = 0;

        for (int i = idStart; i <= idEnd; i++) {
            String currentStr = Integer.toString(i);
            int currentStrLength = currentStr.length();
            for (int j = 0; j < currentStrLength; j++) {
                if (target == currentStr.charAt(j)) {
                    count++;
                }
            }
        }

        return count;
    }
}

