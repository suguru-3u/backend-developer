package org.example.progrem4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8080)) {

            // クライアントからの接続を待ち受け（accept）
            Socket socket = server.accept();
            System.out.println("クライアントとの接続成功！！");

            // クライアントへのメッセージ投信
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // キーボード入力の受け取り
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.println("好きなメッセージを入力してください");
                String input = keyboard.readLine();
                writer.println(input);
                System.out.println("メッセージをクライアントに送信");
                if (input.equals("stop")) break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
