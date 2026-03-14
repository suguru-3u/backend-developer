package org.example.chatApp.progrem2;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum OperationCode {
    CREATIONCHATROOM(1),
    ENTRYTOCHATROOM(2);

    final int operationCode;

    OperationCode(int operationCode) {
        this.operationCode = operationCode;
    }

    public int getValue() {
        return operationCode;
    }

    public static Optional<OperationCode> from(int operationCode) {
        for (OperationCode code : OperationCode.values()) {
            if (code.getValue() == operationCode) return Optional.of(code);
        }
        return Optional.empty();
    }
}

enum OperationState {
    SERVERINIT(0),
    CONCERTMISTRESSES(1),
    OVERCOMPLICATES(2);

    final int operationCode;

    OperationState(int operationCode) {
        this.operationCode = operationCode;
    }

    public int getValue() {
        return operationCode;
    }

    public static Optional<OperationState> from(int operationCode) {
        for (OperationState code : OperationState.values()) {
            if (code.getValue() == operationCode) return Optional.of(code);
        }
        return Optional.empty();
    }
}

// リクエストコードから操作を行うクラスを作成したい。
class TcrpHeader {

    public int roomNameSize;

    public OperationCode operationCode;

    public OperationState operationState;

    public int payloadSize;

    TcrpHeader(byte[] headerBuffer) throws Exception {
        // ただのバイト配列（byte[]）に、便利な『読み取り用ツール』を被せている
        ByteBuffer header = ByteBuffer.wrap(headerBuffer);
        // 0xFFは、負の数を正の数（0〜255）として正しく解釈するためのもの（ビット論理積）
        this.roomNameSize = header.get() & 0xFF;

        // オペレーションコードの取得
        int operation = header.get() & 0xFF;
        Optional<OperationCode> operationCode = OperationCode.from(operation);
        if (operationCode.isEmpty()) throw new Exception("不正なOperationCode");
        System.out.println(operationCode.get());
        this.operationCode = operationCode.get();

        // オペレーションの状態を取得
        int state = header.get() & 0xFF;
        Optional<OperationState> operationState = OperationState.from(state);
        if (operationState.isEmpty()) throw new Exception("不正なStateCode");
        this.operationState = operationState.get();

        // OpPayloadSize (29バイト分) の解析
        byte[] payloadSizeField = new byte[29];
        header.get(payloadSizeField);
        // 簡易的に最後のバイトをサイズとして取得（仕様に合わせて調整が必要）
        this.payloadSize = payloadSizeField[28] & 0xFF;
    }
}

class TcrpBody {

    public OperationCode operationCode;

    public OperationState operationState;

    public String roomName;

    public String payload;

    TcrpBody(DataInputStream in, TcrpHeader tcrpHeader) throws IOException {
        // 2. ボディの受信
        this.operationCode = tcrpHeader.operationCode;

        this.operationState = tcrpHeader.operationState;

        byte[] roomNameBytes = new byte[tcrpHeader.roomNameSize];
        in.read(roomNameBytes);
        this.roomName = new String(roomNameBytes, StandardCharsets.UTF_8);

        byte[] payloadBytes = new byte[tcrpHeader.payloadSize];
        in.read(payloadBytes);
        this.payload = new String(payloadBytes, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "Received: Room=" + roomName + ", Op=" + operationCode.name() + ", State=" + operationState.name() + ", Payload=" + payload;
    }
}


public class Server {

    public static int ROOMNAMESIZE = 1;

    public static int OPERATIONSIZE = 1;

    public static int STATESIZE = 1;

    public static int OPERATIONPAYLOADSIZE = 29;

    // スレッドプールを作成（CPUコア数に合わせて調整）
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    // ユーザーからのチャットルームに関するリクエストを受け付けて対応する
    public static void receiveChatRoomRequest(ServerSocket svsock) {
        try {
            Socket sock = svsock.accept();
            DataInputStream in = new DataInputStream(sock.getInputStream());

            // ヘッダーの受信
            byte[] headerBuffer = new byte[32];
            int read = in.read(headerBuffer);
            if (read != 32) throw new Exception("不正なヘッダー");

            TcrpHeader tcrpHeader = new TcrpHeader(headerBuffer);

            // 2. ボディの受信
            TcrpBody tcrpBody = new TcrpBody(in, tcrpHeader);
            System.out.println(tcrpBody);

            //受信ストリームの終了
            in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        /*
          行うこと
          1. ユーザー空のリクエストを受け付ける
          - チャットルームを作成する関数を用意する
          ユーザーからのリクエストをもとに、チャットルームを作成する
          - ユーザーにチャットルームに参加できるようにする
         */

        // 非同期でアクセスを受け付けるプログラムと
        // ユーザーの情報を管理するプログラムが必要になる
        // 非同期でチャットを行うプログラムを作成する


        ServerSocket svsock = new ServerSocket(9000);
        System.out.println("サーバーの起動完了\nクライアントからの接続を待ちます。");
        receiveChatRoomRequest(svsock);


    }
}

