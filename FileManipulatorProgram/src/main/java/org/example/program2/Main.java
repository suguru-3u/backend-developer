package org.example.program2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * TODO: ストラテジーパターンを使用して、コマンドを追加されても変更に強くする
 * TODO: バリデーション処理を大きくする
 * TODO: Optionalの処理を追加する
 * TODO: ファイルの読み込み処理にバッファ読み込み・ByteBufferを使用するようにする
 */

public class Main {
    private static final int commandIndex = 0;

    private static final int inputPathIndex = 1;

    private static final int outputPathIndex = 2;


    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("引数を設定してください");
            return;
        }

        boolean validationResult = validationCommand(args);
        if (!validationResult) return;

        Path inputFilePath = getFilePath(args[inputPathIndex]);
        Path outputFilePath = getFilePath(args[outputPathIndex]);
        if (inputFilePath == null || outputFilePath == null) return;

        try (RandomAccessFile reader = new RandomAccessFile(inputFilePath.toFile(), "r");
             RandomAccessFile writer = new RandomAccessFile(outputFilePath.toFile(), "rw")) {

            long fileLength = reader.length();

            // ファイルの末尾から先頭に向かってループ
            for (long pos = fileLength - 1; pos >= 0; pos--) {
                reader.seek(pos);        // 読み取り位置を移動
                int b = reader.read();   // 1バイト読み取り
                writer.write(b);         // 書き込み
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Path getFilePath(String path) {
        if (path.isEmpty()) {
            System.out.println("ファイル情報を入力してください");
            return null;
        }

        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            System.out.println("inputするファイル情報の取得に失敗しました");
            return null;
        }
    }

    static boolean validationCommand(String[] args) {
        String command = args[commandIndex];

        if (command.isEmpty()) {
            System.out.println("コマンドを入力してください");
            return false;
        }

        if (!command.equals("reverse") && !command.equals("copy") && !command.equals("duplicate-contents") && !command.equals("replace-string")) {
            System.out.println("コマンドは、「reverse」「copy」「duplicate-contents」「replace-string」のみ受付ています");
            return false;
        }

        if (command.equals("replace-string")) {
            if (args.length != 4) {
                System.out.println("入力引数が合っていません");
                return false;
            }
        }

        if (command.equals("reverse") | command.equals("copy") | command.equals("duplicate-contents")) {
            if (args.length != 3) {
                System.out.println("入力引数が合っていません");
                return false;
            }
        }

        return true;
    }
}
