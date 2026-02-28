package org.example.progrem5;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// ソケットとの接続
// JSONでリクエストを受け取る
// 関数の目次を作成
// JSONでレスポンス

public class Server {
    public static void main(String[] args) throws IOException {
        // ソケットファイルのパス
        Path socketPath = Path.of("/tmp/test_socket.sock");

        // 前回の残骸があれば削除
        Files.deleteIfExists(socketPath);

        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);
        ObjectMapper mapper = new ObjectMapper();

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            serverChannel.bind(address);
            System.out.println("UNIXドメインソケットで待機中: " + socketPath);

//            while (true) {
            // 接続を受け入れる
            try (SocketChannel clientChannel = serverChannel.accept();
                 InputStream input = Channels.newInputStream(clientChannel);
                 OutputStream output = Channels.newOutputStream(clientChannel)) {

                // 1. BufferedReaderで「改行」が来るまで読む
                // (InputStreamReaderで文字コード指定を忘れずに)
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

                // readLine()は改行を見つけるとすぐに戻ってくる（EOFを待たない）
                String line = reader.readLine();

                System.out.println("接続を受け付けました"); // acceptの直後

                if (line != null) {
                    // 2. 文字列からJSONへ変換
                    RpcRequest request = mapper.readValue(line, RpcRequest.class);
                    System.out.println("受信成功: " + request.method);

                    // 3. レスポンスを返す（ソケットはまだ生きているので成功する）
                    mapper.writeValue(output, request);
                    output.flush();
                }

                // 3. この try ブロックを抜けるときに clientChannel が close される
                // これにより、Node.js 側にデータの終わりが伝わる

            } catch (EOFException e) {
                // クライアントが切断した場合は正常終了として扱う
                System.out.println("クライアントが接続を閉じました");
            } catch (Exception e) {
                // それ以外のパースエラーなどを表示
                e.printStackTrace();
                System.err.println("パースエラー: " + e.getMessage());
            }
//            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}

class RpcRequest {
    public int id;

    @JsonProperty("param_types")
    public List<String> paramTypes;

    public int[] params;

    public String method;

    public boolean exit() {
        return method.equals("exit");
    }
}


