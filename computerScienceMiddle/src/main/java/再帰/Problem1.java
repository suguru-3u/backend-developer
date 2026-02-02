package 再帰;

/**
 * ウイルス感染
 *
 * <p>
 * アメリカにある得体の知れないウィルスが外国から持ち込まれ、毎日 2 倍のスピードで感染を拡大しています。
 * 経過日数 day が与えられるので、day 日目のみに感染した人の数を返す、infectedPeople という関数を作成してください。
 * <p>
 *
 * 入力のデータ型： integer day
 * 出力のデータ型： integer
 *
 * infectedPeople(1) --> 2
 * infectedPeople(2) --> 4
 * infectedPeople(3) --> 8
 * infectedPeople(15) --> 32768
 */

class Problem1 {
    public static int infectedPeople(int day){
        // 関数を完成させてください
        if(day == 0) return 1;

        return infectedPeople(day - 1) * 2;
    }
}

