package 再帰.最大公約数;

import java.util.Objects;

/**
 * 文字列の圧縮
 *
 * <p>
 * Hodge は文章を短く表示するアプリを作成しており、文字が連続して 2 回以上続く場合は文字を数字に置き換えようと考えています。
 * アルファベットで書かれた文字列 s が与えられるので、再帰を使って連続で続いた文字を数字に置き換える、stringCompression という関数を作成してください。
 * <p>
 * <p>
 * 入力のデータ型： string s
 * 出力のデータ型： string
 * <p>
 * stringCompression("aaabbb") --> a3b3
 * stringCompression("aaabbbc") --> a3b3c
 * stringCompression("aaabbbCCL") --> a3b3C2L
 * stringCompression("aaabbbCCLL") --> a3b3C2L2
 * stringCompression("abceeaaaddbbb") --> abce2a3d2b3
 * stringCompression("aaabbbaaaaccaaaaba") --> a3b3a4c2a4ba
 * stringCompression("a") --> a
 */

class Problem6 {
    public static String stringCompression(String s) {
        return stringCompressionHelper(s, 0, s.length() - 1);
    }

    public static String stringCompressionHelper(String baseStr, int index, int maxIndex) {
        if (maxIndex < index) return "";
        if (maxIndex == index) return baseStr.substring(index);

        int count = checkSameStrCount(baseStr, baseStr.substring(index, index + 1), index + 1, maxIndex, 1);
        String str = baseStr.substring(index, index + 1);
        String str1 = count == 1 ? str : str + count;

        return str1 + stringCompressionHelper(baseStr, index + count, maxIndex);
    }

    public static int checkSameStrCount(String baseStr, String target, int index, int maxIndex, int count) {
        if (maxIndex < index) return count;
        if (!Objects.equals(target, baseStr.substring(index, index + 1))) return count;
        return checkSameStrCount(baseStr, target, index + 1, maxIndex, count) + 1;
    }
}

