package org.example.chatApp.progrem2;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Server {

    public static int ROOMNAMESIZE = 1;

    public static int OPERATIONSIZE = 1;

    public static int STATESIZE = 1;

    public static int OPERATIONPAYLOADSIZE = 29;

    public static void main(String[] args){

        /*
          行うこと
          1. ユーザー空のリクエストを受け付ける
          - チャットルームを作成する関数を用意する
          ユーザーからのリクエストをもとに、チャットルームを作成する
          - ユーザーにチャットルームに参加できるようにする
         */

        try(ServerSocket svsock = new ServerSocket(9000);){

            System.out.println("サーバーの起動完了\nクライアントからの接続を待ちます。");
            Socket sock = svsock.accept();

            DataInputStream in = new DataInputStream(sock.getInputStream());

            // ヘッダーの受信
            byte[] headerBuffer = new byte[32];
            int read = in.read(headerBuffer);
            if(read != 32) throw new Exception("不正なヘッダー");

            // ただのバイト配列（byte[]）に、便利な『読み取り用ツール』を被せている
            ByteBuffer header = ByteBuffer.wrap(headerBuffer);
            // 0xFFは、負の数を正の数（0〜255）として正しく解釈するためのもの（ビット論理積）
            int roomNameSize = header.get() & 0xFF;
            int operation = header.get() & 0xFF;
            int state = header.get() & 0xFF;

            // OpPayloadSize (29バイト分) の解析
            byte[] payloadSizeField = new byte[29];
            header.get(payloadSizeField);
            // 簡易的に最後のバイトをサイズとして取得（仕様に合わせて調整が必要）
            int payloadSize = payloadSizeField[28] & 0xFF;

            // 2. ボディの受信
            byte[] roomNameBytes = new byte[roomNameSize];
            in.read(roomNameBytes);
            String roomName = new String(roomNameBytes, StandardCharsets.UTF_8);

            byte[] payloadBytes = new byte[payloadSize];
            in.read(payloadBytes);
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);

            System.out.println("Received: Room=" + roomName + ", Op=" + operation + ", State=" + state + ", Payload=" + payload);

            //受信ストリームの終了
            in.close();


        }catch (Exception e){
            e.printStackTrace();
        }


    }
}

