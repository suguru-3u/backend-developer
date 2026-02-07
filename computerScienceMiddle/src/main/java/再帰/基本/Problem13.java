package 再帰.基本;

/**
 * 文字列の逆表示
 *
 * <p>
 * Catherine は与えられた単語や文章を逆側から読み上げる遊びを友達とやっています。
 * 文字列 string が与えられるので、string を反転した文字列を返す reverseString という関数を再帰を使って定義してください。
 * <p>
 * <p>
 * 入力のデータ型： string s
 * 出力のデータ型： string
 * <p>
 * reverseString("abcd") --> dcba
 * reverseString("recursion") --> noisrucer
 * reverseString("I am a software engineer") --> reenigne erawtfos a ma I
 */

class Problem13 {
    public static String reverseString(String s) {
        return reverseStringHelper(s, 0, s.length() - 1);
    }

    public static String reverseStringHelper(String s, int index, int maxIndex) {
        if (index == maxIndex) return s.substring(maxIndex);
        return reverseStringHelper(s,index + 1, maxIndex) + s.charAt(index);
    }
}

