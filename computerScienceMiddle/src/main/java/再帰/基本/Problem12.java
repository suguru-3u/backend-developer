package 再帰.基本;

/**
 * 羊を数える
 *
 * <p>
 * Michaela は今日昼寝をしてしまったため、なかなか寝付くことができません。
 * そこで羊を 1 匹ずつ数えて眠りを待つことにしました。
 * 0 以上の整数 count が与えられるので、夢の中で count 匹羊を数える、sheeps という関数を作成してください。
 * <p>
 *
 * 入力のデータ型： integer count
 * 出力のデータ型： string
 *
 * sheeps(2) --> 1 sheep ~ 2 sheep ~
 * sheeps(4) --> 1 sheep ~ 2 sheep ~ 3 sheep ~ 4 sheep ~
 * sheeps(5) --> 1 sheep ~ 2 sheep ~ 3 sheep ~ 4 sheep ~ 5 sheep ~
 * sheeps(10) --> 1 sheep ~ 2 sheep ~ 3 sheep ~ 4 sheep ~ 5 sheep ~ 6 sheep ~ 7 sheep ~ 8 sheep ~ 9 sheep ~ 10 sheep ~
 */

class Problem12 {
    public static String sheeps(int count){
        if(count == 1) return "1 sheep ~ ";
        return sheeps(count - 1) + String.format("%s sheep ~ ", count);
    }
}

