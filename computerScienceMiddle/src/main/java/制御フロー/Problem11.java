package 制御フロー;

/**
 * カエサルの暗号
 *
 * <p>
 * 紀元前 70 年頃、古代ローマの軍事的指導者ガイウス・ユリウス・カエサル（Gaius Iulius Caesar）は
 * 秘密文書を敵に解読されないために文字列に含まれる全ての単語を数文字分シフトさせる方法を思いつきました。
 * 小文字によって構成された文字列 message、自然数 n が与えられるので、暗号を作成する caesarCipher という関数を作成してください。
 * z の次は a に戻ることに注意してください。
 * また空白によってメッセージが読み取られないよう、返される文字列の空白は全て取り除いてください。
 * <p>
 * 入力のデータ型： string message, integer n
 * 出力のデータ型： string
 * <p>
 * caesarCipher("i love you",0) --> iloveyou
 * caesarCipher("i love you",1) --> jmpwfzpv
 * caesarCipher("i love you",2) --> knqxgaqw
 * caesarCipher("the future belongs to those who believe in the beauty of their dreams",2) --> vjghwvwtgdgnqpiuvqvjqugyjqdgnkgxgkpvjgdgcwvaqhvjgktftgcou
 * caesarCipher("it is during our darkest moments that we must focus to see the light",5) --> nynxizwnsltzwifwpjxyrtrjsyxymfybjrzxykthzxytxjjymjqnlmy
 * caesarCipher("do not go where the path may lead go instead where there is no path and leave a trail",10) --> nyxydqygrobodrozkdrwkivoknqysxcdokngrobodroboscxyzkdrkxnvokfokdbksv
 */

class Problem11 {
    public static String caesarCipher(String message, int n) {
        String cleanStr = message.replace(" ", "");
        StringBuilder cryptoStr = new StringBuilder();
        int strLength = cleanStr.length();
        n = n % 26;

        int step = 0;
        while (step < strLength) {
            int currentChar = cleanStr.charAt(step);
            int shiftedChar = currentChar + n;
            char ch = (char) (122 < shiftedChar ? (shiftedChar - 122) + 96 : shiftedChar);
            cryptoStr.append(ch);
            step++;
        }

        return cryptoStr.toString();
    }

    public static String caesarCipherV2(String message, int n) {
        String cleanStr = message.replace(" ", "");
        StringBuilder cryptoStr = new StringBuilder();
        n = n % 26; // シフトが26を超える場合の処理

        for (int i = 0; i < cleanStr.length(); i++) {
            char currentChar = cleanStr.charAt(i);
            char shiftedChar = (char) ((currentChar - 'a' + n) % 26 + 'a');
            cryptoStr.append(shiftedChar);
        }

        return cryptoStr.toString();
    }
}

