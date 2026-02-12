package 制御フロー;

/**
 * 割り切れない
 *
 * <p>
 * notDivisibleNumbers(x,y) は、1 から x までの整数を y で割った時に余りがある数を見つけ、"-" で区切って文字列として返す関数です。
 * 例えば、x = 10, y = 3 の場合、1,2,3,4,5,6,7,8,9,10 のうち 3 で割った時に余りがある数は
 * 1,2,4,5,7,8,10 なので、"-" で区切って "1-2-4-5-7-8-10" という文字列が返されます。
 * <p>
 * ただし、x = 1 かつ y = 1 の場合、1 を割る数が 1 しかないため、余りがある数は存在せず、
 * 文字列として返すものがないため、関数は定義されません。また、x や y が負の数や小数である場合、この関数は正しく動作しません。
 * <p>
 * 入力のデータ型： integer x, integer y
 * 出力のデータ型： string
 * <p>
 * notDivisibleNumbers(20,3) --> 1-2-4-5-7-8-10-11-13-14-16-17-19-20
 * notDivisibleNumbers(50,4) --> 1-2-3-5-6-7-9-10-11-13-14-15-17-18-19-21-22-23-25-26-27-29-30-31-33-34-35-37-38-39-41-42-43-45-46-47-49-50
 * notDivisibleNumbers(100,2) --> 1-3-5-7-9-11-13-15-17-19-21-23-25-27-29-31-33-35-37-39-41-43-45-47-49-51-53-55-57-59-61-63-65-67-69-71-73-75-77-79-81-83-85-87-89-91-93-95-97-99
 */

class Problem14 {
    public static String notDivisibleNumbers(int x, int y) {
        if (x == 1 && y == 1) return "";
        if (x < 1 || y < 1) return "";
        StringBuilder notDivisibleStr = new StringBuilder();

        for (int i = 1; i <= x; i++) {
            if (i % y == 0) continue;
            notDivisibleStr.append(i);
            notDivisibleStr.append("-");
        }

        if (notDivisibleStr.charAt(notDivisibleStr.length() - 1) == '-') {
            return notDivisibleStr.substring(0, notDivisibleStr.length() - 1);
        }

        return notDivisibleStr.toString();
    }
}

