package 抽象化;

/**
 * ドメインの切り取り
 *
 * <p>
 * メールアドレスが与えられるので、メソッドチェーンを用いてドメイン名を大文字で切り出す
 * upperCaseDomain という関数を作成してください。
 * <p>
 *
 * 入力のデータ型： string email
 * 出力のデータ型： string
 *
 * upperCaseDomain("bjacobi@example.com") --> EXAMPLE.COM
 * upperCaseDomain("christine37@example.net") --> EXAMPLE.NET
 * upperCaseDomain("deontae.braun@metz.org") --> METZ.ORG
 * upperCaseDomain("elwyn.leannon@example.com") --> EXAMPLE.COM
 * upperCaseDomain("carter88@gmail.com") --> GMAIL.COM
 * upperCaseDomain("amaya.kohler@yahoo.com") --> YAHOO.COM
 */

class Problem2 {
    public static String upperCaseDomain(String email){
        // 関数を完成させてください
        return email.substring(email.indexOf("@") + 1).toUpperCase();
    }
}

