package 再帰.その他;

/**
 * 投資の計算
 *
 * <p>
 * Kathy は現在価格 goalMoney ドルの土地の購入するために、
 * 年利 interest（0 < interest < 100）%の金融商品に capitalMoney ドル投資しようと計画しています。
 * goalMoney, interest, capitalMoney が与えられるので、何年後に土地の購入ができるかを返す、
 * howLongToReachFundGoal という関数を再帰によって作成してください。
 * <p>
 * なお、毎年得られた利益は同商品に再投資するとし、
 * 土地の価格は経過する年数が偶数（0 を含む）の時は 2%、奇数の時は 3% 上昇します。
 * また、人の寿命は 80 歳未満と仮定し、80 年以上かかる時は 80 としてください。
 * <p>
 * 入力のデータ型： integer capitalMoney, integer goalMoney, integer interest
 * 出力のデータ型： integer
 * <p>
 * howLongToReachFundGoal(5421,10421,5) --> 27
 * howLongToReachFundGoal(5421,30,30) --> 0
 * howLongToReachFundGoal(600,10400,7) --> 67
 * howLongToReachFundGoal(32555,5200000,12) --> 58
 * howLongToReachFundGoal(50,35000,65) --> 14
 * howLongToReachFundGoal(10,350000,2) --> 80
 * howLongToReachFundGoal(20000,10050000,30) --> 27
 */

class Problem5 {
    public static int howLongToReachFundGoal(int capitalMoney, int goalMoney, int interest) {
        return howLongToReachFundGoalHelper(capitalMoney, goalMoney, interest, 0);
    }

    public static int howLongToReachFundGoalHelper(int capitalMoney, int goalMoney, int interest, int year) {
        if (80 < year) return 80;
        if (goalMoney <= capitalMoney) return year;
        int landPrice = calculateLandPrice(goalMoney, year);
        int financialInstruments = calculateFinancialInstruments(capitalMoney, interest);
        return howLongToReachFundGoalHelper(capitalMoney + financialInstruments, landPrice, interest, year + 1);
    }

    public static int calculateLandPrice(int landPrice, int year) {
        if(year == 0) return (int) (landPrice * 1.02);
        return (int) (year % 2 == 0 ? landPrice * 1.02 : landPrice * 1.03);
    }

    public static int calculateFinancialInstruments(int capitalMoney, int interest) {
        return (interest * capitalMoney) / 100;
    }
}