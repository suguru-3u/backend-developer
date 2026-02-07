package 再帰.基本;

/**
 * 文字列の合体
 *
 * <p>
 * Valerie は与えられた文字カードを並べて、単語を作るカードゲームを開発しています。
 * ゲーム中に、2 人のプレイヤーの手札を交互に並べる処理をつける予定です。
 * 空白を含まない、サイズが同じ文字列 s1 と s2 が与えられるので、
 * それぞれの手札のカードを s1->s2 の順序で交互に組み合わせる、mergeString という関数を再帰を使って作成してください。
 * <p>
 * <p>
 * 入力のデータ型： string s1, string s2
 * 出力のデータ型： string
 * <p>
 * mergeString("abc","def") --> adbecf
 * mergeString("hello","world") --> hweolrllod
 * mergeString("a","b") --> ab
 */

class Problem7 {
    public static String mergeString(String s1, String s2) {
        return mergeStringHelper(s1, s2, s1.length() - 1);
    }

    public static String mergeStringHelper(String s1, String s2, int index) {
        if (index == -1) return "";
        return mergeStringHelper(s1, s2, index - 1) + s1.charAt(index) + s2.charAt(index);
    }

    public static String swapPosition(String s) {
        // 関数を完成させてください
        return swapPositionHelper(s, 0);
    }

    public static String swapPositionHelper(String s, int index) {
        if (s.length() < index - 1) return s.substring(index);
        String first = s.substring(index, 1);
        String second = s.substring(index + 1, index + 2);
        return second + first + swapPositionHelper(s.substring(index), index + 2);
    }
}

