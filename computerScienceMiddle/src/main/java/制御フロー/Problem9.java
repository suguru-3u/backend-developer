package 制御フロー;

/**
 * 回文
 *
 * <p>
 * Charly と彼の友人たちは、前から読んでも後ろから読んでも同じになる「回文」を作る遊びをしています。
 * このために、ある文字列が回文かどうかをチェックする isPalindrome という関数を作成する必要があります。
 * この関数では、スペースは無視し、大文字と小文字は区別しないとします。
 * <p>
 * 例えば、"Top spot" という文字列でこの関数を使用すると、スペースを除いて大文字小文字の区別をなくすと "Topspot" となります。
 * これは前から読んでも "Topspot"、後ろから読んでも "topspoT" となり、回文であるため、関数は true を返すべきです。
 * <p>
 * 別のテストケースとして "Was it a cat I saw" があります。
 * 同様にスペースを除去し、大文字小文字を区別しないと "Wasitacatisaw" となり、
 * これも前後どちらから読んでも変わらないため、回文です。従って、この文字列に対しても関数は true を返すべきです。
 * <p>
 * 入力のデータ型： string stringInput
 * 出力のデータ型： bool
 * <p>
 * isPalindrome("kayak") --> true
 * isPalindrome("recursion") --> false
 * isPalindrome("Top spot") --> true
 * isPalindrome("Was it a cat I saw") --> true
 * isPalindrome("ad") --> false
 */

class Problem9 {
    public static boolean isPalindrome(String stringInput) {
        String cleanedStr = stringInput.replace(" ", "");
        StringBuilder reversedStr = new StringBuilder();

        int step = cleanedStr.length() - 1;
        while (0 <= step) {
            reversedStr.append(cleanedStr.charAt(step));
            step--;
        }

        return reversedStr.toString().equalsIgnoreCase(cleanedStr);
    }
}

