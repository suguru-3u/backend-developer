package 再帰.その他;

/**
 * 数字を分割して足す
 *
 * <p>
 * Juan は小学 1 年生の息子に足し算を教える方法として、桁数を分解して足し合わせるという方法を思いつきました。
 * 自然数 digits が与えられるので、桁数を分解して足し合わせる、splitAndAdd という関数を再帰を使って作成してください。例えば、98 は、9 + 8 = 17 となり、9728 は、9 + 7 + 2 + 8 = 26 となります。
 *
 * <p>
 * <p>
 * 入力のデータ型： integer digits
 * 出力のデータ型： integer
 * <p>
 * splitAndAdd(19) --> 10
 * splitAndAdd(898) --> 25
 * splitAndAdd(23387) --> 23
 * splitAndAdd(1066) --> 13
 * splitAndAdd(546125) --> 23
 */

class Problem1 {
    public static int splitAndAdd(int digits) {
        if (digits < 10) return digits;
        return digits % 10 + splitAndAdd(digits / 10);
    }
}

