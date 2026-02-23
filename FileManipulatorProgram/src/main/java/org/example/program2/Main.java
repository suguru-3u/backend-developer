package org.example.program2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Map;
import java.util.function.Function;


class CommandFactory {

    private static final int COMMAND_INDEX = 0;

    private static final Map<String, Function<String[], Command>> REGISTRY = Map.of(
            "reverse", ReverseCommand::new,
            "copey", CopeyCommand::new,
            "replace", ReplaceCommand::new
            // 将来: "copy", CopyCommand::new
    );

    public static Command create(String[] args) {
        if (args.length == 0) throw new IllegalArgumentException("引数が空です");

        String commandName = args[COMMAND_INDEX];
        var factory = REGISTRY.get(commandName);

        if (factory == null) {
            throw new IllegalArgumentException("未対応のコマンドです: " + commandName);
        }
        return factory.apply(args);
    }
}

interface Command {
    void execute();
}

class ReplaceCommand implements Command {

    private static final int ARGS_SIZE = 5;

    private static final int INPUT_PATH_INDEX = 1;

    private static final int OUTPUT_PATH_INDEX = 2;

    private static final int TARGET_STR_INDEX = 3;

    private static final int REPLACE_STR_INDEX = 4;

    private final Path inputPath;

    private final Path outputPath;

    private final String targetStr;

    private final String replaceStr;

    public ReplaceCommand(String[] args) {
        if (args.length != ARGS_SIZE) throw new IllegalArgumentException("コマンド引数が合っていません");
        this.inputPath = FilePath.of(args[INPUT_PATH_INDEX]).value();
        this.outputPath = FilePath.of(args[OUTPUT_PATH_INDEX]).value();
        this.targetStr = args[TARGET_STR_INDEX];
        this.replaceStr = args[REPLACE_STR_INDEX];
    }

    @Override
    public void execute() {
        ReplaceFileProcessor.execute(
                this.inputPath,
                this.outputPath,
                this.targetStr,
                this.replaceStr
        );
    }
}

class ReplaceFileProcessor {
    static void execute(Path input, Path output, String targetStr, String replaceStr) {
        try (BufferedReader br = Files.newBufferedReader(input);
             BufferedWriter wr = Files.newBufferedWriter(output)) {

            String line;
            while ((line = br.readLine()) != null) {
                String replaceResult = line.replace(targetStr, replaceStr);
                wr.newLine();
                wr.write(replaceResult);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class CopeyCommand implements Command {

    private static final int ARGS_SIZE = 3;

    private static final int INPUT_PATH_INDEX = 1;

    private static final int OUTPUT_PATH_INDEX = 2;

    private final Path inputPath;

    private final Path outputPath;

    public CopeyCommand(String[] args) {
        if (args.length != ARGS_SIZE) throw new IllegalArgumentException("コマンド引数が合っていません");
        this.inputPath = FilePath.of(args[INPUT_PATH_INDEX]).value();
        this.outputPath = FilePath.of(args[OUTPUT_PATH_INDEX]).value();
    }

    @Override
    public void execute() {
        CopeyFileProcessor.execute(
                this.inputPath,
                this.outputPath
        );
    }
}

class CopeyFileProcessor {
    static void execute(Path input, Path output) {
        try {
            // 手軽..一度にユーザー空間メモリにファイルの内容が書き込まれるため、巨大なファイルではメモリ不足になるかも
            Files.copy(input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ReverseCommand implements Command {

    private static final int ARGS_SIZE = 3;

    private static final int INPUT_PATH_INDEX = 1;

    private static final int OUTPUT_PATH_INDEX = 2;

    private final Path inputPath;

    private final Path outputPath;

    public ReverseCommand(String[] args) {
        if (args.length != ARGS_SIZE) throw new IllegalArgumentException("reverseコマンドには入力・出力パスが必要です");
        if (args[INPUT_PATH_INDEX].equals(args[OUTPUT_PATH_INDEX]))
            throw new IllegalArgumentException("入力と出力が同じです");
        this.inputPath = FilePath.of(args[INPUT_PATH_INDEX]).value();
        this.outputPath = FilePath.of(args[OUTPUT_PATH_INDEX]).value();
    }

    @Override
    public void execute() {
        ReverseFileProcessor.execute(inputPath, outputPath);
    }
}

class ReverseFileProcessor {

    private static final int BUFFER_SIZE = 64 * 1024;

    static void execute(Path input, Path output) {
        try (FileChannel in = FileChannel.open(input);
             FileChannel out = FileChannel.open(output, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            long size = in.size();
            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            for (long pos = size; pos > 0; ) {
                int readSize = (int) Math.min(BUFFER_SIZE, pos);
                pos -= readSize;

                buffer.clear();
                int totalRead = 0;
                while (totalRead < readSize) {
                    int bytesRead = in.read(buffer, pos + totalRead);
                    if (bytesRead == -1) {
                        throw new IOException("予期せぬファイルの末尾に到達しました");
                    }
                    totalRead += bytesRead;
                }
                buffer.flip();
                ReverseAlgorithm.reverse(buffer);
                out.write(buffer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

class ReverseAlgorithm {
    static void reverse(ByteBuffer buffer) {
        int left = 0;
        int right = buffer.limit() - 1;
        while (left < right) {
            byte temp = buffer.get(left);
            buffer.put(left, buffer.get(right));
            buffer.put(right, temp);
            left++;
            right--;
        }
    }
}

record FilePath(Path value) {
    public static FilePath of(String pathString) {
        if (pathString == null || pathString.isBlank()) throw new IllegalArgumentException("パスが空です");
        try {
            return new FilePath(Paths.get(pathString));
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("不正なパス: " + pathString, e);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            Command command = CommandFactory.create(args);
            command.execute();
        } catch (Exception e) {
            // 【修正】エラー内容を確実に出力
            System.err.println("エラーが発生しました: " + e);
            // 必要に応じて e.printStackTrace();
        }
    }
}