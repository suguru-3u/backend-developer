package org.example.progrem2;

import java.io.*;
import java.net.Socket;

public class ParentProcess {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("127.0.0.1", 8080)) {

            // サーバーからのメッセージ受信
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                String input = reader.readLine();
                System.out.println("[サーバーからのメッセージ受信]" + input);
                if (input.equals("stop")) break;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}