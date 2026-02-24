package org.example.progrem3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
    public static void main(String[] args) {
        // 8888番ポートで待ち受け
        try (DatagramSocket socket = new DatagramSocket(8888)) {
            System.out.println("サーバー起動：受信を待っています...");

            // 受信用のバッファ（箱）を用意
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // 受信（データが来るまでここで止まります）
            socket.receive(packet);

            // 届いたデータを文字列に変換して表示
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("受信内容: " + message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
