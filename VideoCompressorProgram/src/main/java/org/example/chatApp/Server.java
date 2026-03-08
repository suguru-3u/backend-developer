package org.example.chatApp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * TODO：リファクタリングとログの表示フォーマット整理、考慮不足を考える
 */

public class Server {

    public static int USERNAME_INDEX = 1;

    private static final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    // スレッドプールを作成（CPUコア数に合わせて調整）
    private static final  ExecutorService executor = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {
        // 8888番ポートで待ち受け
        try (DatagramSocket socket = new DatagramSocket(8888)) {
            System.out.println("サーバー起動：受信を待っています...");

            ChatUserList chatUserList = new ChatUserList(socket);

            // スケジュール処理
            scheduleRemoveUser(chatUserList);

            while (true) {
                // 受信用のバッファ（箱）を用意
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // 受信（データが来るまでここで止まります）Mainスレッドの役割
                socket.receive(packet);

                // 別スレッドでメッセージを送信する
                executor.submit(() -> sendChatMessage(packet, chatUserList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendChatMessage(DatagramPacket packet, ChatUserList chatUserList) {
        // --- ここから送信者の情報を取得 ---
        InetAddress senderAddress = packet.getAddress(); // 送信者のIPアドレス
        int senderPort = packet.getPort();
        System.out.println("チャットメッセージの送信者情報： " + senderAddress + ": " + senderPort);

        System.out.println("チャットメッセージを受け付けました。");
        int userNameLength = Integer.parseInt(new String(packet.getData(), 0, USERNAME_INDEX));

        // 届いたデータを文字列に変換して表示
        String userName = new String(packet.getData(), USERNAME_INDEX, userNameLength);
        System.out.println("送信者名: " + userName);

        int chatMessageStartIndex = USERNAME_INDEX + userNameLength;

        // 届いたデータを文字列に変換して表示
        String chatMessage = new String(packet.getData(), chatMessageStartIndex, packet.getLength() - chatMessageStartIndex);
        System.out.println("チャットメッセージ: " + chatMessage);

        // メッセージの送信処理
        System.out.println("チャットメッセージを送信します");
        chatUserList.add(userName, senderAddress, senderPort);
        chatUserList.sendMessage(packet, senderPort);
        System.out.println("チャットメッセージを送信しました");
    }

    // しばらく送信していないユーザーを削除
    public static void scheduleRemoveUser(ChatUserList chatUserList) {
        System.out.println("スケジューラーを登録します");

        // try-with-resourcesを使わずに登録する
        cleaner.scheduleAtFixedRate(() -> {
            try {
                System.out.println("定期実行開始します");
                chatUserList.addDeleteUser();
                chatUserList.cleanChatUser();
                System.out.println("定期実行が完了しました");
            } catch (Exception e) {
                // 定期実行スレッド内で例外が起きると以降の実行が止まるため、catchしておくのが安全です
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
}

class ChatUserList {

    private final ConcurrentHashMap<String, ChatUser> chatUserLists;

    private final DatagramSocket socket;

    private final List<String> deleteChatUserList;

    ChatUserList(DatagramSocket socket) {
        this.socket = socket;
        this.chatUserLists = new ConcurrentHashMap<>();
        this.deleteChatUserList = new ArrayList<>();
    }

    public void add(
            String name,
            InetAddress inetAddress,
            int port
    ) {
        ChatUser user = new ChatUser(name, inetAddress, port);
        chatUserLists.put(name, user);
    }

    public void sendMessage(DatagramPacket packet, int senderPort) {
        chatUserLists.forEach((name, chatUser) -> {
            if (chatUser.port == senderPort) {
                return;
            }

            DatagramPacket replyPacket = new DatagramPacket(packet.getData(), packet.getLength(), chatUser.inetAddress, chatUser.port);
            chatUserLists.put(name, chatUser.successSendMessage());
            try {
                System.out.println(chatUser.name + "さんへメッセージを送信");
                this.socket.send(replyPacket);
            } catch (IOException e) {
                System.out.println(chatUser.name + "さんへのメッセージ送信に失敗しました");
            }
        });
    }

    public void addDeleteUser() {
        chatUserLists.forEach((name, chatUser) -> {
            if (chatUser.checkSendMessageEnable()) {
                deleteChatUserList.add(name);
            }
        });
    }

    public void cleanChatUser() {
        deleteChatUserList.forEach(chatUserLists::remove);
    }
}

class ChatUser {

    public String name;

    public LocalTime lastSendMessageTime;

    public LocalTime LimitSendMessageTIme;

    public int failureSendMessageCount;

    public InetAddress inetAddress;

    public int port;

    ChatUser(String name, InetAddress inetAddress, int port) {
        this.name = name;
        this.lastSendMessageTime = LocalTime.now();
        this.LimitSendMessageTIme = this.lastSendMessageTime.plusMinutes(10);
        this.failureSendMessageCount = 0;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public ChatUser successSendMessage() {
        return new ChatUser(
                this.name,
                this.inetAddress,
                this.port
        );
    }

    private boolean checkSendMessageTime() {
        return LocalTime.now().isAfter(this.LimitSendMessageTIme);
    }

    public boolean checkSendMessageEnable() {
        return checkSendMessageTime();
    }
}
