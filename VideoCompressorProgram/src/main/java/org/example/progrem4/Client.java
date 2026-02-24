package org.example.progrem4;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String message = "こんにちは、UDPです！";
            byte[] buffer = message.getBytes();

            // 送り先の設定（自分自身の8888番ポート）
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 8888);

            // 送信！
            socket.send(packet);
            System.out.println("メッセージを送信しました。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
