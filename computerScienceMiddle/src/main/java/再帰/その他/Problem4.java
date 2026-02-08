package 再帰.その他;

/**
 * 約数
 *
 * <p>
 * 正の整数を引数として受け取り、その数の約数をすべてハイフンで区切って返す
 * 関数 divisor を再帰を用いて定義してください。
 * この関数を呼び出した際に返される文字列の形式は以下のようになります。
 * <p>
 * 例えば、divisor(12) を呼び出すと、文字列 "1-2-3-4-6-12" が返されます。
 * 最初の数値は必ず 1 で、その後の数値は昇順で約数が表示されます。最後の数値は引数で与えられた整数そのものです。
 * <p>
 * divisor(28) --> 1-2-4-7-14-28
 * divisor(29) --> 1-29
 * divisor(720) --> 1-2-3-4-5-6-8-9-10-12-15-16-18-20-24-30-36-40-45-48-60-72-80-90-120-144-180-240-360-720
 */

class Problem4 {
    public static String divisor(int number) {
        return divisorHelper(number, 1);
    }

    public static String divisorHelper(int num, int counter) {
        if (num == counter) return String.valueOf(counter);
        String addStr = num % counter == 0 ? counter + "-" : "";
        return addStr + divisorHelper(num, counter + 1);
    }
}

