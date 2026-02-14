package 制御フロー;

/**
 * FizzBuzz
 *
 * <p>
 * fizzBuzz 関数は、引数として与えられた自然数 n 以下の数列を生成します。
 * この数列において、3で割り切れる数は "Fizz"、5 で割り切れる数は "Buzz"、3 と 5 の両方で割り切れる数は
 * "FizzBuzz" と置き換えられます。
 * ただし、割り切れない場合はその数をそのまま表示します。この関数を完成させてください。
 * 数列を文字列として返します。ただし、各数値と数値の間はハイフン - で区切られます。
 * <p>
 * 入力のデータ型： integer n
 * 出力のデータ型： string
 * <p>
 * fizzBuzz(7) --> 1-2-Fizz-4-Buzz-Fizz-7
 * fizzBuzz(16) --> 1-2-Fizz-4-Buzz-Fizz-7-8-Fizz-Buzz-11-Fizz-13-14-FizzBuzz-16
 * fizzBuzz(30) --> 1-2-Fizz-4-Buzz-Fizz-7-8-Fizz-Buzz-11-Fizz-13-14-FizzBuzz-16-17-Fizz-19-Buzz-Fizz-22-23-Fizz-Buzz-26-Fizz-28-29-FizzBuzz
 */

class Problem15 {
    public static String fizzBuzz(int n) {
        StringBuilder fizzBuzzStr = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            if(i % 3 == 0 && i % 5 ==0){
                fizzBuzzStr.append("FizzBuzz");
            }else if(i % 5 ==0){
                fizzBuzzStr.append("Buzz");
            } else if (i % 3 == 0) {
                fizzBuzzStr.append("Fizz");
            }else{
                fizzBuzzStr.append(i);
            }

            if(i < n){
                fizzBuzzStr.append("-");
            }
        }
        return fizzBuzzStr.toString();
    }
}

