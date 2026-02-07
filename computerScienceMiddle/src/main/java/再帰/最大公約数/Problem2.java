package 再帰.最大公約数;

/**
 * 仮想通貨
 *
 * <p>
 * 仮想通貨で大儲けした L さんは、ビットコインとイーサリアムを、同じ枚数ずつできるだけ多くの人に配りたいと思いました。
 * ビットコインとイーサリアムの枚数がそれぞれ x, y で与えられるので、配れる人数を返す、maximumPeople という関数を作成してください。
 * <p>
 * 例えば、ビットコイン 12 枚とイーサリアム 18 枚を配る場合、ビットコインを 6 人に 2 枚ずつ、イーサリアムを 6 人に 3 枚ずつ配ることができます。
 * <p>
 * <p>
 * 入力のデータ型： integer x, integer y
 * 出力のデータ型： integer
 * <p>
 * maximumPeople(12,18) --> 6
 * maximumPeople(30,242) --> 2
 * maximumPeople(1029,1071) --> 21
 * maximumPeople(3180,1908) --> 636
 */

class Problem2 {
    public static int maximumPeople(int x, int y) {
        if (y == 0) return x;
        if (x <= y) {
            y = y % x;
        } else {
            x = x - y;
        }
        return maximumPeople(x, y);
    }

    // これでもいける
    public static int maximumPeople2(int x, int y){
        if (y == 0) return x;
        return maximumPeople2(y, x % y);
    }
}

