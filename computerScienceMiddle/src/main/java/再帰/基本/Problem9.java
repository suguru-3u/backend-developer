package 再帰.基本;

/**
 * 掛け算（再帰）
 *
 * <p>
 * Yorke は計算機アプリを作成しており、掛け算を行う機能を追加しています。
 * この機能に拡張性をつけるために、再帰で掛け算の処理を行う予定です。
 * 整数 x、y が与えられるので、x と y の掛け算の結果を返す、product という関数を再帰によって作成してください。
 * <p>
 * <p>
 * 入力のデータ型： integer x, integer y
 * 出力のデータ型： integer
 * <p>
 * product(5,3) --> 15
 * product(3,5) --> 15
 * product(5,-1) --> -5
 * product(-9,8) --> -72
 * product(-10,-5) --> 50
 * product(0,2) --> 0
 * product(-9,0) --> 0
 * product(0,0) --> 0
 */

class Problem9 {
    public static int product(int x, int y) {
        return productHelper1(Math.abs(x), Math.abs(y)) * productHelper2(x,y);
    }

    public static int productHelper1(int x, int y) {
        if (y == 0) return 0;
        return productHelper1(x, y - 1) + x;
    }

    public static int productHelper2(int x, int y){
        float sSign = Math.signum(x);
        float ySign = Math.signum(y);
        if(sSign == 1.0 && ySign == 1.0){
            return 1;
        } else if(sSign == -1.0 && ySign == -1.0){
            return 1;
        } else {
            return -1;
        }
    }

    // これでもいける..
    public static int product2(int x, int y){
        if(y == 0) return 0;
        if(y < 0) return product(x , y + 1) - x;
        else return product(x, y - 1) + x;
    }
}

