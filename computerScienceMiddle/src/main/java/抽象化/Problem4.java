package 抽象化;

/**
 * 複利の計算
 *
 * <p>
 * 元金 capital と年数 year が与えられるので、複利を使って将来の合計金額を計算します。
 * 以下の要件に基づいて、関数 calculateGoalMoney を作成してください。
 * 元金（capital）と期間（year）を使用して、複利の式 P(1+i)n を使って将来の最終的な金額を計算します。
 * 最初の元金が偶数の場合は年利率が 2％、奇数の場合は年利率が 3％ とします。
 * 計算結果は小数点以下を切り捨て、整数として返します。
 *
 * 例として複利計算の公式 P(1 + i)n を使用します。
 * ここで、P は投資初期金額、i は年利、n は期間を指します。
 * 例えば、元金 300 万円を年利 3% で 20 年間運用すると、
 * 最終的に得られる金額は 300 × (1 + 0.03)20 = 5,418,321 円となります。
 * <p>
 *
 * 入力のデータ型： integer capital, integer year
 * 出力のデータ型： integer
 *
 * calculateGoalMoney(2,3) --> 2
 * calculateGoalMoney(4,7) --> 4
 * calculateGoalMoney(16,24) --> 25
 * calculateGoalMoney(35,47) --> 140
 */

class Problem4 {
    public static int calculateGoalMoney(int capital, int year) {
        double rate = calculateAnnualInterestRate(capital);
        double compoundInterest = calculateCompoundInterest(capital,rate, year);
        return (int) Math.floor(compoundInterest);
    }

    private static double calculateAnnualInterestRate(int capital) {
        return capital % 2 == 0 ? 0.02 : 0.03;
    }

    private static double calculateCompoundInterest(int capital, double rate, int year){
        return capital * Math.pow(1 + rate, year);
    }
}

