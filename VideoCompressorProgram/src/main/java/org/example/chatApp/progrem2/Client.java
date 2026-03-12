package org.example.chatApp.progrem2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Client {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 9000);
             OutputStream out = socket.getOutputStream()) {
            String roomName = "sampleRoom";
            String userName = "sampleUser";

            byte[] roomNameBytes = roomName.getBytes(StandardCharsets.UTF_8);
            byte[] payloadBytes = userName.getBytes(StandardCharsets.UTF_8);

            // 1. ヘッダーの作成 (32バイト固定)
            ByteBuffer header = ByteBuffer.allocate(32);
            header.put((byte) roomNameBytes.length); // RoomNameSize (1 byte)
            header.put((byte) 1);                   // Operation: 1 (Create)
            header.put((byte) 0);                   // State: 0 (Request)

            // OpPayloadSize (29 bytes)
            // 29バイトすべてを数値に使うのは稀なため、ここでは後方に数値を詰め、残りを0埋めします
            byte[] payloadSizeField = new byte[29];
            payloadSizeField[28] = (byte) payloadBytes.length;
            header.put(payloadSizeField);

            // 2. ヘッダーとボディを送信
            out.write(header.array());
            out.write(roomNameBytes);
            out.write(payloadBytes);
            out.flush();

            System.out.println("Room creation request sent.");
            // 書き込みの削除
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}