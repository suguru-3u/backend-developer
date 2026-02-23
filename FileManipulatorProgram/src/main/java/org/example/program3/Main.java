package org.example.program3;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.function.Function;


class ConverterCommand {

    private static final int ARGS_SIZE = 3;

    private static final int INPUT_PATH_INDEX = 1;

    private static final int OUTPUT_PATH_INDEX = 2;

    private final Path inputPath;

    private final Path outputPath;

    public ConverterCommand(String[] args) {
        if (args.length != ARGS_SIZE) throw new IllegalArgumentException("コマンド引数が合っていません");
        this.inputPath = FilePath.of(args[INPUT_PATH_INDEX]).value();
        this.outputPath = FilePath.of(args[OUTPUT_PATH_INDEX]).value();
    }

    public void execute() {
        ReplaceFileProcessor.execute(
                this.inputPath,
                this.outputPath
        );
    }
}

class ReplaceFileProcessor {

    private static final Parser PARSER = Parser.builder().build();

    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    static void execute(Path input, Path output) {
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {

            /**
             * 処理の内容
             * 1. パース段階：PARSER.parseReader(reader)
             * この一行を実行している間、ライブラリは以下の動きをします。
             *
             * インプットファイルを最後まで全て読み込みます。
             * 読み込んだ内容を解析し、ドキュメント全体の構造を 「Node（木構造）」としてメモリ上に全て構築し終えるまで、次の行へは進みません。
             * つまり、このメソッドが完了した時点では、Markdownの全内容（構造化されたデータ）がユーザーメモリ（JVMのヒープ空間）にドーンと居座っている状態になります。
             * ここがメモリ消費のピークです。 「Nodeが作られ次第書き出す」という並行処理はこの段階では行われません。
             */
            Node document = PARSER.parseReader(reader);

            /**
             * 処理の内容
             * 2. レンダリング段階：RENDERER.render(document, writer)
             * パースが完了し、メモリ上に「完成したNodeのツリー」がある状態で、この一行が実行されます。
             *
             * ここでは、すでにメモリ上にあるNodeを上から順に走査（スキャン）します。
             * 走査しながら、対応するHTML文字列を生成し、writer を通じてアウトプットファイルへ書き出していきます。
             */
            RENDERER.render(document, writer);

            // 明示的にフラッシュ（BufferedWriterのclose時に自動で行われるが念のため）
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            ConverterCommand command = new ConverterCommand(args);
            command.execute();
        } catch (Exception e) {
            // 【修正】エラー内容を確実に出力
            System.err.println("エラーが発生しました: " + e);
            // 必要に応じて e.printStackTrace();
        }
    }
}