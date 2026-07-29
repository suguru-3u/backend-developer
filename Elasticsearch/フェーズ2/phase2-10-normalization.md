# 第10章　表記ゆれ対応 ― 正規化・読み・n-gram

> **前章との役割分担**
> 第9章のシノニムは「イヤホン＝イヤフォンは同義」と**辞書に明示**して揺れを吸収しました。しかし世の中の揺れは、いちいち辞書に書ききれないほど機械的なものも多くあります。全角と半角、大文字と小文字、ひらがなとカタカナ、ローマ字、そして「辞書に載っていない語の部分一致」。これらは**規則で自動的に**畳み込むのが筋がよい。この章はその「正規化（normalization）」の技術です。
>
> **辞書（シノニム）＝意味の揺れ、規則（正規化）＝表記の揺れ**。両者は補完関係にあります。

---

## 学習目標

この章を終えると、次のことができるようになります。

- 表記ゆれとシノニムの役割分担を説明できる
- `icu_normalizer` で全角／半角・大文字／小文字を正規化できる（char filter として使う理由も理解）
- `icu_folding`（さらに強い畳み込み）と `icu_transform`（カタカナ⇄ひらがな）を使い分けられる
- `kuromoji_readingform`（読み・ローマ字）、`kuromoji_stemmer`（長音）、`kuromoji_iteration_mark` の役割を説明できる
- **正規化は index 時と search 時に対称に**入れるべき理由を説明できる（シノニムとの違い）
- `ngram` / `edge_ngram` による部分一致・サジェストの仕組みと副作用を理解する
- マルチフィールドで「正規化・読み・n-gram」を1フィールドにまとめて持つ設計ができる

---

## この章のサンプルデータ（前提）

第8・9章と同じ `products`（6件）を使います。第2章で **icu プラグイン**、第4章で **kuromoji** を導入済みである前提です。想定する揺れの例：

| 正規語 | ありがちな入力の揺れ |
|---|---|
| イヤホン | ｲﾔﾎﾝ（半角） ／ いやほん（ひらがな） ／ iyahon（ローマ字） |
| Bluetooth | ｂｌｕｅｔｏｏｔｈ（全角） ／ BLUETOOTH（大文字） |
| パソコン | ヤホ・パソ のような**部分一致**での検索 |

---

## 10.1　表記ゆれとシノニムの役割分担

第9章と本章は、どちらも「違う入力でも同じ商品に当てたい」という目的は同じです。しかし手段が異なります。

```
             揺れの種類ごとに使い分ける
 ┌─────────────────────────────┬──────────────────────────────┐
 │ シノニム（第9章）            │ 正規化（第10章）              │
 │ 「意味が同じ別の語」          │ 「同じ語の書き方の違い」        │
 │ 例: イヤホン ↔ イヤフォン     │ 例: ｲﾔﾎﾝ → イヤホン           │
 │ 手段: 辞書に1件ずつ登録       │ 手段: 変換規則を1つ入れれば全語に効く │
 └─────────────────────────────┴──────────────────────────────┘
```

正規化は**規則1つが全トークンに効く**のが強みです。全角半角のような揺れを辞書で列挙するのは非現実的で、正規化の出番になります。

---

## 10.2　Unicode 正規化 ― icu_normalizer

`icu_normalizer` は Unicode の正規化を行います。既定の `nfkc_cf` は、次を一度に処理します。

- **NFKC**（互換等価の正規化）：全角英数「Ｂｌｕｅｔｏｏｔｈ」→ 半角「Bluetooth」、半角カナ「ｲﾔﾎﾝ」→ 全角「イヤホン」など
- **cf（case folding）**：大文字小文字を畳む（「BLUETOOTH」→「bluetooth」）

### char filter として使うのがおすすめ

`icu_normalizer` は **char filter（トークナイズ前）** と **token filter（トークナイズ後）** の両方で使えますが、日本語では **char filter として kuromoji_tokenizer の前**に置くのが定石です。半角カナなどをトークナイズ前に整えておくと、kuromoji が正しく形態素解析できるためです。

```json
PUT /products_norm_demo
{
  "settings": {
    "analysis": {
      "char_filter": {
        "normalize": { "type": "icu_normalizer", "name": "nfkc_cf", "mode": "compose" }
      },
      "analyzer": {
        "kuromoji_normalized": {
          "type": "custom",
          "char_filter": ["normalize"],
          "tokenizer": "kuromoji_tokenizer",
          "filter": ["kuromoji_baseform", "lowercase"]
        }
      }
    }
  }
}
```

確認：

```json
POST /products_norm_demo/_analyze
{ "analyzer": "kuromoji_normalized", "text": "ｲﾔﾎﾝ" }
```

`イヤホン` に正規化されたトークンが返れば成功です。半角入力でも全角の商品名にヒットするようになります。

---

## 10.3　icu_folding と icu_transform

### icu_folding ― さらに強く畳む

`icu_folding` は `icu_normalizer` より**踏み込んだ畳み込み**を行い、アクセント記号・幅・大文字小文字などの区別を広く取り除きます（英語圏の「café → cafe」のような処理を Unicode 全域で行うイメージ）。多言語データやアクセント付き文字を含む場合に有効です。ただし畳みすぎると別語まで同一視される恐れがあるため、`unicode_set_filter` で対象外の文字を指定して調整します。

### icu_transform ― カタカナ⇄ひらがな

`icu_transform` は**文字体系の変換（トランスリタレーション）**を行います。日本語では「カタカナ→ひらがな」で、ひらがな入力とカタカナ表記を揃えるのに便利です。

```json
"filter": {
  "kata2hira": { "type": "icu_transform", "id": "Katakana-Hiragana" }
}
```

これを index 時・search 時の両方に入れておくと、「いやほん」でも「イヤホン」でもヒットするようになります（`Any-Latin` などラテン文字化の変換 id もあります）。

---

## 10.4　kuromoji 系フィルタ ― 読み・長音・踊り字

kuromoji プラグインには、日本語特有の揺れに効くトークンフィルタが揃っています。

| フィルタ | 役割 | 例 |
|---|---|---|
| `kuromoji_readingform` | 読み（カナ）を出力。`use_romaji: true` でローマ字 | イヤホン → `iyahon` |
| `kuromoji_stemmer` | 末尾の長音（ー）を除去して長音ゆれを吸収 | サーバー → サーバ |
| `kuromoji_iteration_mark` | 踊り字（々・ゝ等）を正規化 | 日々 → 日日 |
| `kuromoji_baseform` | 活用語を基本形へ | 走っ → 走る |
| `ja_stop` | 日本語のストップワード除去 | 「の」「する」等 |

### ローマ字検索の考え方

`kuromoji_readingform`（`use_romaji: true`）を使うと、日本語トークンをローマ字トークンに変換できます。これを**専用のサブフィールド**（例：`name.reading`）に持たせ、検索側でも同じ変換をかければ、「iyahon」というローマ字入力で「イヤホン」の商品に当てられます。

```json
"filter": {
  "romaji": { "type": "kuromoji_readingform", "use_romaji": true }
}
```

---

## 10.5　正規化は「両側対称」に入れる（重要）

第9章では、シノニムは主に**検索時だけ**に入れました。しかし正規化は考え方が逆で、**index 時と search 時の両方に、同じ変換を入れる**のが原則です。

理由は単純で、正規化は「トークンを正規形に揃える」処理だからです。片側だけに入れると、揃った側と揃っていない側で形が食い違い、一致しなくなります。

```
✗ 片側だけ正規化
   index側:  イヤホン（正規化なし: ｲﾔﾎﾝ のまま保存）
   search側: イヤホン（正規化あり）
        → 形が違って一致しない

✓ 両側正規化（対称）
   index側:  ｲﾔﾎﾝ → イヤホン
   search側: ｲﾔﾎﾝ → イヤホン
        → 同じ正規形どうしで一致する
```

- **正規化（icu 系・kuromoji 系）** … index・search 両方に対称に入れる
- **シノニム（意味展開）** … 原則 search 時のみ（第9章）

この使い分けが、揺れ対策の設計の背骨になります。

---

## 10.6　部分一致 ― n-gram と edge_ngram

ここまでは「同じ語の書き方を揃える」話でした。最後は「語の一部で当てる」部分一致です。形態素解析では、辞書にない語や語の途中からの一致が苦手なため、**n-gram** で補います。

### n-gram ― 任意位置の部分一致

文字列を N 文字ずつの窓でスライスしてトークン化します。`min_gram`〜`max_gram` の範囲で切り出します。

```json
"tokenizer": {
  "bigram": { "type": "ngram", "min_gram": 2, "max_gram": 2 }
}
```

「イヤホン」→ `イヤ` `ヤホ` `ホン`。ユーザーが「ヤホ」と打っても、`ヤホ` トークンで一致します。形態素解析では拾いにくい**語中からの部分一致**に強いのが特徴です。

> **`index.max_ngram_diff`**：`max_gram - min_gram` が既定で 1 までに制限されています。幅を広げたいときは、この設定を明示的に上げる必要があります。

### edge_ngram ― 前方一致（サジェスト向き）

先頭からの n-gram だけを作ります。検索ボックスの入力補完（オートコンプリート）に向きます。

```json
"tokenizer": {
  "edge": { "type": "edge_ngram", "min_gram": 1, "max_gram": 10 }
}
```

「パソコン」→ `パ` `パソ` `パソコ` `パソコン`。入力途中の「パソ」で候補を出せます。

> **コラム：n-gram の副作用**
> 部分一致は強力ですが代償があります。(1) **インデックスが肥大化**する（トークン数が激増）。(2) **誤ヒット**が増える（意味を無視して文字の並びだけで当たる）。(3) スコアが直感的でなくなる。そのため n-gram は**専用のサブフィールド**に限定し、メインは形態素解析、補助として n-gram、という組み合わせが定石です。また、edge_ngram はインデックス側だけに適用し、検索側は素の analyzer にする（`search_analyzer` を分ける）ことで、入力語がさらに n-gram 分割されて誤爆するのを防ぎます。

---

## 10.7　マルチフィールドでまとめる

実務では、1つの `name` に対して**用途別のサブフィールド**を並べ、クエリ側で使い分けます（第3章のマルチフィールドの応用）。

```json
PUT /products_multi
{
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "kuromoji_normalized",          // 正規化つき形態素（メイン）
        "fields": {
          "ngram":   { "type": "text", "analyzer": "ngram_analyzer" },    // 部分一致
          "reading": { "type": "text", "analyzer": "romaji_analyzer" }    // ローマ字
        }
      }
    }
  }
}
```

検索時は `multi_match`（第8章）で束ね、正規化フィールドを主・n-gram とローマ字を従にします。

```json
GET /products_multi/_search
{
  "query": {
    "multi_match": {
      "query": "ヤホ",
      "fields": ["name^3", "name.ngram", "name.reading"],
      "type": "best_fields",
      "tie_breaker": 0.2
    }
  }
}
```

こうすると「正規化された正確な一致を最優先しつつ、部分一致やローマ字でも取りこぼさない」検索になります。第8章の重み付け、第9章のシノニム、本章の正規化・部分一致が、ここで一本の検索にまとまります。

---

## まとめ

- **シノニム＝意味の揺れ（辞書）、正規化＝表記の揺れ（規則）**。役割分担して両方使う。
- `icu_normalizer`（`nfkc_cf`）で全角半角・大文字小文字を正規化。日本語では **char filter として kuromoji の前**に置くのが定石。
- `icu_folding`（さらに強い畳み込み）、`icu_transform`（カタカナ⇄ひらがな等）で揺れをさらに吸収。
- kuromoji 系では `kuromoji_readingform`（読み・ローマ字）、`kuromoji_stemmer`（長音）、`kuromoji_iteration_mark`（踊り字）が効く。
- **正規化は index・search の両側に対称に**入れる（シノニムが原則 search 時のみなのと対照的）。
- `ngram`（任意位置の部分一致）／`edge_ngram`（前方一致・サジェスト）は強力だが、肥大化・誤ヒットの副作用があるため**専用サブフィールド**に限定する。
- マルチフィールド＋`multi_match` で「正規化・部分一致・読み」を1つの検索に束ねる。

---

## 理解度チェック

**Q1.** シノニムと正規化は、それぞれどんな種類の揺れに向いていますか。

<details><summary>解答</summary>

シノニムは「イヤホン↔イヤフォン」のような**意味が同じ別の語**（辞書に列挙する）。正規化は「ｲﾔﾎﾝ→イヤホン」のような**同じ語の書き方の違い**（規則1つで全語に効く）。表記ゆれを辞書で列挙するのは非現実的なので、正規化で機械的に畳みます。
</details>

**Q2.** `icu_normalizer` を token filter ではなく **char filter** として kuromoji の前に置くのはなぜですか。

<details><summary>解答</summary>

半角カナや全角英数などをトークナイズ**前**に正規形へ整えておくと、kuromoji tokenizer が正しく形態素解析できるためです。トークナイズ後に正規化すると、分割自体が揺れの影響を受けてしまいます。
</details>

**Q3.** 正規化フィルタは index 時と search 時のどちらに入れるべきですか。理由も。

<details><summary>解答</summary>

**両方に対称に**入れます。正規化はトークンを正規形へ揃える処理なので、片側だけに入れると、揃った側と揃っていない側で形が食い違い一致しなくなります。これはシノニム（原則 search 時のみ）との重要な違いです。
</details>

**Q4.** n-gram（bigram）で「イヤホン」をトークン化するとどうなり、どんな利点と副作用がありますか。

<details><summary>解答</summary>

`イヤ` `ヤホ` `ホン` に分割されます。利点は「ヤホ」のような**語中からの部分一致**に強いこと。副作用は、インデックスの肥大化、意味を無視した誤ヒットの増加、スコアの不直感さ。そのため専用のサブフィールドに限定して使います。
</details>

**Q5.** edge_ngram を使うとき、`search_analyzer` を index 用と分けるのはなぜですか。

<details><summary>解答</summary>

検索語まで edge_ngram で分割してしまうと、入力語の断片どうしが広く一致して誤爆します。インデックス側だけ edge_ngram を適用し、検索側は素の analyzer にすることで、「入力語そのもの」で前方一致の候補を引けるようにします。
</details>

---

## 次章予告

第11章「**集計（Aggregations／ファセット検索）**」では、検索の視点を「1件ずつ探す」から「**まとめて数える・集計する**」へ広げます。カテゴリごとの件数、価格帯の分布、在庫の割合――ECサイトの絞り込み（ファセット）や管理画面のダッシュボードを支える機能です。`products` の `category` / `tags` / `price` / `in_stock` を材料に、メトリック集計・バケット集計・入れ子集計、そして「検索結果とファセット件数を両立させる」`post_filter` の使い方を学びます。

---

## キーワード

正規化（normalization） ／ 表記ゆれ ／ icu プラグイン ／ `icu_normalizer`（`nfkc_cf`） ／ NFKC ／ case folding ／ char filter ／ `icu_folding` ／ `unicode_set_filter` ／ `icu_transform`（Katakana-Hiragana / Any-Latin） ／ `kuromoji_readingform`（`use_romaji`） ／ `kuromoji_stemmer`（長音） ／ `kuromoji_iteration_mark` ／ `ja_stop` ／ 両側対称の正規化 ／ `ngram`（bigram） ／ `edge_ngram` ／ `index.max_ngram_diff` ／ 部分一致 ／ オートコンプリート ／ マルチフィールド ／ `search_analyzer`
