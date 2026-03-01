package org.example.chatApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * TODO：リファクタリングとログの表示フォーマット整理、考慮不足を考える
 */

public class Client {

    public static int USERNAME_INDEX = 1;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("あなたの名前を入力してください");
            String userName = keyboard.readLine();

            System.out.println("送信したいメッセージを送信してください");
            String chatMessage = keyboard.readLine();

            String sendMessage = String.valueOf(userName.length()) + userName + chatMessage;
            byte[] buffer = sendMessage.getBytes();

            // 送り先の設定（自分自身の8888番ポート）
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 8888);

            // 送信！
            socket.send(packet);
            System.out.println("メッセージを送信しました。");

            // 受信用のバッファ（箱）を用意
            byte[] receiveBuffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            // 受信（データが来るまでここで止まります）
            socket.receive(receivePacket);

            System.out.println("チャットメッセージを受け付けました。");
            int userNameLength = Integer.parseInt(new String(packet.getData(), 0, USERNAME_INDEX));

            // 届いたデータを文字列に変換して表示
            String userName1 = new String(packet.getData(), USERNAME_INDEX, userNameLength);
            System.out.println("送信者名: " + userName1);

            int chatMessageStartIndex = USERNAME_INDEX + userNameLength;

            // 届いたデータを文字列に変換して表示
            String chatMessage1 = new String(packet.getData(), chatMessageStartIndex, packet.getLength() - chatMessageStartIndex);
            System.out.println("チャットメッセージ: " + chatMessage1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

