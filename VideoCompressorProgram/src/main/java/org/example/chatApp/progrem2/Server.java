package org.example.chatApp.progrem2;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
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

            //データを受信
            // 1. まずヘッダー（4バイト）を読んでサイズを取得
            int length = in.read();

// 2. 指定されたサイズ分だけボディを読み込む
            byte[] body = new byte[length];
            in.readFully(body);

//            int roomNameSize = Integer.parseInt(new String(body, 0, ROOMNAMESIZE));

            //受信データを読み込んだサイズまで切り詰め
            String roomName = new String(body);

            //バイト配列を文字列に変換して表示
            System.out.println("「"+ roomName +"」を受信しました。");

            //受信ストリームの終了
            in.close();


        }catch (Exception e){
            e.printStackTrace();
        }


    }
}

