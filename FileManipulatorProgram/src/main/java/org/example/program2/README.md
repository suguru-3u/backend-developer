# 仕様

以下のコマンドとその機能を提供する プログラムを作成してください。

引数の入力が正しいかどうかをチェックするバリデータを必ず記述しましょう。

- reverse inputpath outputpath: inputpath にあるファイルを受け取り、outputpath に inputpath の内容を逆にした新しいファイルを作成します。
- copy inputpath outputpath: inputpath にあるファイルのコピーを作成し、outputpath として保存します。
- duplicate-contents inputpath n: inputpath にあるファイルの内容を読み込み、その内容を複製し、複製された内容を inputpath に
  n 回複製します。
- replace-string inputpath needle newstring: inputpath にあるファイルの内容から文字列 'needle' を検索し、'needle'
  の全てを 'newstring' に置き換えます。

# ファイルを読み取る・書き込む仕組み

## ファイルの読み込み

ファイルの読み込みには、Files、BufferedReader、InputStreamReader、FileInputStream、FileChannelなどさまざまな方法が存在する。
それぞれの内容の比較表と実装方法のサンプルを記載する。 現在主流となっている書き方（try-with-resources文を使用した安全な書き方）のサンプルをまとめました。

### 1. ファイル読み込み方法の比較表

| 分類        | クラス名                  | 主な用途         | 特徴                                         |
|-----------|-----------------------|--------------|--------------------------------------------|
| **最新・推奨** | **Files**             | テキスト・バイナリ全般  | 最も簡単。数行で全内容を読み込める。Java 7以降。                |
| **テキスト**  | **BufferedReader**    | 大きなテキストファイル  | **一行ずつ**読み込むのに最適。メモリ効率が良い。                 |
| **パース**   | **Scanner**           | 数値や区切り文字の解析  | スペースやカンマで区切られたデータを数値として読み込むのに便利。           |
| **文字変換**  | **InputStreamReader** | 文字コード指定が必要な時 | `FileInputStream`と組み合わせて、Shift-JISなどを指定する。 |
| **バイナリ**  | **FileInputStream**   | 画像、音声、実行ファイル | データを「バイト単位」でそのまま読み込む。                      |
| **高速・特殊** | **FileChannel**       | 巨大なファイル、高速処理 | メモリマップドファイルなど、高度で特殊な操作用。                   |

---

### 2. 使い方のサンプルコード

現代のJavaでは、使い終わった後に自動でファイルを閉じてくれる **`try-with-resources`** という書き方を使うのが鉄則です。

#### A. 一番おすすめ：`Files.readAllLines` (全行読み込み)

ファイルがそれほど大きくない（数MB程度まで）なら、これが最も楽です。

```java
import java.nio.file.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("test.txt");

        // 全行をリストとして取得
        List<String> lines = Files.readAllLines(path);
        lines.forEach(System.out::println);
    }
}

```

#### B. メモリに優しい：`BufferedReader` (一行ずつ)

巨大なファイルを読み込むときは、メモリを節約するためにこの方法を使います。

```java
import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        Path path = Paths.get("large_file.txt");

        // FilesクラスからReaderを作成するのが現代流
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

#### C. 特定の文字コードを指定：`InputStreamReader`

「古いWindowsで作られたShift-JISのファイル」などを読み込む場合に必要です。

```java
import java.io.*;

public class Main {
    public static void main(String[] args) {
        File file = new File("sjis_file.txt");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "Shift_JIS"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

#### D. データの解析：`Scanner`

ファイル内の数値を合計したい時などに便利です。

```java
import java.util.Scanner;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Scanner scanner = new Scanner(new File("data.txt"))) {
            while (scanner.hasNext()) {
                if (scanner.hasNextInt()) {
                    int num = scanner.nextInt();
                    System.out.println("数値を発見: " + num);
                } else {
                    scanner.next(); // 数値以外はスキップ
                }
            }
        }
    }
}

```

## ファイルの書き込み

書き込み（出力）も、読み込みと同じ考え方で整理できます。基本的には**「新世代のFiles」**か、**「効率のBufferedWriter」**、そして**
「バイナリのOutputStream」**の3つを押さえれば完璧です。

### 1. ファイル書き込み方法の比較表

| 分類        | クラス名                 | 主な用途        | 特徴                             |
|-----------|----------------------|-------------|--------------------------------|
| **最新・推奨** | **Files**            | テキスト・バイナリ全般 | 1行で書き込み完了。追記や新規作成の切り替えも簡単。     |
| **テキスト**  | **BufferedWriter**   | 大量のテキスト出力   | バッファリングにより、何度も書き込む際の処理が高速。     |
| **便利出力**  | **PrintWriter**      | ログ出力、整形出力   | `println()` が使えるため、改行や数値の整形が楽。 |
| **バイナリ**  | **FileOutputStream** | 画像、音声、PDFなど | データをバイト単位でそのまま保存する。            |
| **高速・特殊** | **FileChannel**      | 巨大なデータ、高速同期 | 大規模システムのログ保存など、極限の性能が必要な場合。    |

---

### 2. 書き込みのサンプルコード

読み込みと同様に、`try-with-resources` 文を使用して、使い終わったら確実にファイルを閉じるようにします。

#### A. 一番おすすめ：`Files.writeString` (全内容書き込み)

Java 11以降で最もスマートな方法です。

```java
import java.nio.file.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("output.txt");
        String content = "こんにちは、Javaの書き込みテストです。";

        // 上書き保存（ファイルがなければ作成される）
        Files.writeString(path, content);

        // 追記したい場合（オプションを追加）
        // Files.writeString(path, "追記します", StandardOpenOption.APPEND);
    }
}

```

#### B. 効率重視：`BufferedWriter` (大量の行を出力)

ループの中で何度も書き込む場合は、これを使わないと動作が重くなります。

```java
import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        Path path = Paths.get("log.txt");

        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            for (int i = 0; i < 100; i++) {
                bw.write("行番号: " + i);
                bw.newLine(); // OSに合わせた改行コードを挿入
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

#### C. 人間に優しい：`PrintWriter` (整形して出力)

`System.out.println` と同じ感覚でファイルに書き込めます。

```java
import java.io.*;

public class Main {
    public static void main(String[] args) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream("report.txt"))) {
            pw.println("--- 実行レポート ---");
            pw.printf("合計金額: %,d 円%n", 1500000); // カンマ区切りなどの整形が可能
            pw.println("完了しました。");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

```

---

### 💡 覚えておくべき重要ポイント

1. **上書きか追記か**:
   デフォルトは「上書き」です。追記したい場合は `StandardOpenOption.APPEND`（Filesクラス）や、
   `new FileOutputStream(file, true)` のように `true` を指定する必要があります。
2. **文字コード**:
   何も指定しないと現在のOS標準（最近のJavaならUTF-8）になります。特定の文字コードで書きたい場合は、読み込み時と同様に
   `OutputStreamWriter` を挟みます。
3. **フラッシュ（Flush）**:
   `BufferedWriter` などは、ある程度データが溜まるまで書き込みを待ちます。プログラムが途中で強制終了するとデータが消える可能性があるため、重要な箇所では
   `flush()` を呼んで強制的に書き込ませることもあります（`close()` 時には自動で呼ばれます）。
