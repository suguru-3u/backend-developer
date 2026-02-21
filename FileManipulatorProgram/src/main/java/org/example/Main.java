package org.example;

import java.util.Random;
import java.util.Scanner;

//TIP コードを<b>実行</b>するには、<shortcut actionId="Run"/> を押すか
// ガターの <icon src="AllIcons.Actions.Execute"/> アイコンをクリックします。
public class Main {
    public static void main(String[] args) {

        // Scannerクラスを生成し、System.inを渡す
        Scanner scanner = new Scanner(System.in);

        System.out.print("あなたが好きな数字を入力してください: ");
        int minNum = scanner.nextInt();
        System.out.println("入力された値: " + minNum);

        System.out.print("先ほど入力した数字以上の値を入力してください: ");
        int maxNum = scanner.nextInt();
        System.out.println("入力された値: " + maxNum);

        if(maxNum <= minNum) {
            System.out.print("入力された数字が、最初に入力した数値より大きくありません");
            return;
        }

        System.out.println("入力ありがとうございます。今から: " + minNum + " 〜 "  + maxNum + "の間でランダムな数字を生成しますので当ててください");

        Random rand = new Random();
        int answerNum = rand.nextInt((maxNum - minNum) + 1) + minNum;
        while (true){
            System.out.print("生成された数字予測して入力してください: ");
            int inputNum = scanner.nextInt();
            if(inputNum == answerNum) break;
            System.out.print("ハズレ");
        }
        System.out.print("おめでとうございます。当たりました！！");

        scanner.close(); // 必要に応じて閉じるが、標準入力を閉じる際は注意
    }
}