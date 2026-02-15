package 制御フロー;

/**
 * 回文（数値）
 *
 * <p>
 * Jack はある会社に入社し、新しい社員 ID を発行することになりました。
 * ID は数字のみで作られ、必ず回文でなければならないという制約がついています。
 * 新しい ID である自然数 n が与えられるので、それが回文になっているかどうかを調べる
 * isPalindromeInteger という関数を作成してください。
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： bool
 *
 * isPalindromeInteger(12222) --> false
 * isPalindromeInteger(12321) --> true
 * isPalindromeInteger(22782) --> false
 * isPalindromeInteger(189981) --> true
 * isPalindromeInteger(1) --> true
 * isPalindromeInteger(987823) --> false
 */

class Problem17 {
    public static boolean isPalindromeInteger(int n){
        String palindromeStr = String.valueOf(n);
        StringBuilder reversStr = new StringBuilder();

        for(int i = palindromeStr.length() - 1; 0 <= i ; i--){
            reversStr.append(palindromeStr.charAt(i));
        }

        return palindromeStr.contentEquals(reversStr);
    }

    public static boolean isPalindromeIntegerV2(int n) {
        String palindromeStr = String.valueOf(n);
        String reversedStr = new StringBuilder(palindromeStr).reverse().toString();

        return palindromeStr.equals(reversedStr);
    }
}

