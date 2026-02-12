package 制御フロー;

/**
 * 出席管理
 *
 * <p>
 * R 大学ではどの授業でも 3 回以上欠席すると、単位を取得できない制度です。
 * Participate を表す P と Absence を表す A によって構成される文字列 string が与えられるので、
 * 単位取得可能であれば pass、不可能であれば fail を返す、doYouFail という関数を作成してください。
 * <p>
 * 入力のデータ型： string string
 * 出力のデータ型： string
 * <p>
 * doYouFail("AAPPAP") --> fail
 * doYouFail("AAPPAA") --> fail
 * doYouFail("PAPPA") --> pass
 */

class Problem13 {
    public static String doYouFail(String string) {
        int failCount = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == 'A') failCount++;
            if (failCount == 3) return "fail";
        }
        return "pass";
    }
}

