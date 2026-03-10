package org.example.chatApp.progrem2;

import java.io.DataOutputStream;
import java.net.Socket;

public class Client {

    public static void main(String[] args){
        try(Socket socket = new Socket("localhost",9000);) {

            //送信ストリームの取得
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //送信データ
            String sendData = "雑談部屋";

            //文字列をUTF-8形式のバイト配列に変換して送信
            out.write(sendData.getBytes("UTF-8"));

            // 書き込みの削除
            out.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
