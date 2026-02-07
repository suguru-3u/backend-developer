package 再帰.基本;

/**
 * 文字列の長さ（再帰）
 *
 * <p>
 * Gerry は入力された文字の文字数をカウントするアプリを作成しています。
 * このアプリに拡張性をつけるために、再帰で文字をカウントする予定です。
 * 入力された文字列 string が与えられるので、再帰を使って文字数を返す、lenString という関数を作成してください。
 * ただし、Python の len や、JavaScript の length は使わずに解いてください。
 * <p>
 *
 * 入力のデータ型： string string
 * 出力のデータ型： integer
 *
 * lenString("hello world") --> 11
 * lenString("hello") --> 5
 * lenString("a") --> 1
 * lenString("Recursion") --> 9
 */

class Problem3 {
    public static int lenString(String string){
        if(string.isEmpty()) return 0;
        return lenString(string.substring(0, string.length() - 1)) + 1;
    }
}

