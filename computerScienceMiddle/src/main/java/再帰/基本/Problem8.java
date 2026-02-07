package 再帰.基本;

/**
 * 偶奇の入れ替え
 *
 * <p>
 * Stacey は文字の奇数番目と偶数番目を入れ替えても、単語や文章として成り立つのか気になりました。
 * 空白を含まない文字列 s が与えられるので、インデックスの偶数番目と奇数番目を入れ替える、
 * swapPosition という関数を再帰を使って作成してください。
 * <p>
 *
 * 入力のデータ型： string s
 * 出力のデータ型： string
 *
 * swapPosition("abcd") --> badc
 * swapPosition("abcdefgh") --> badcfehg
 * swapPosition("ab") --> ba
 * swapPosition("abcde") --> badce
 */

class Problem8 {
    public static String swapPosition(String s){
        return swapPositionHelper(s, s.length(), 0, "");
    }

    // インデックスを追跡する引数indexを追加します
    // 末尾最適化のために、抜き出した文字列を追加する引数outputも追加します
    public static String swapPositionHelper(String s, int size, int index, String output){
        // index が文字列サイズ - 1 以上になったら再帰を終了します
        if (index >= size - 1) {
            // 文字数が奇数の場合に備えて条件分岐をします。
            // size が偶数の時は、output をそのまま返します。
            // size が奇数の時は、output に s の最後の文字を足して返します。
            if(size % 2 == 0) {
                return output;
            }
            else {
                return output + s.charAt(s.length()-1);
            }
        }
        // index は1つおきに追跡します。偶数番目と奇数番目を入れ替えるので、s[index + 1] + s[index]を output に追加します
        return swapPositionHelper(s, size, index + 2, output + s.charAt(index + 1) + s.charAt(index));
    }
}

