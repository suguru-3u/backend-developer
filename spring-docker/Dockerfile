# --- ビルドステージ --------------------------------------------------
# ビルド用のベースイメージ
FROM gradle:8.7-jdk17-alpine AS build

# セキュリティのため、非rootユーザーを作成して切り替え
RUN addgroup demogroup; adduser  --ingroup demogroup --disabled-password demo
USER demo

# 作業ディレクトリを設定
WORKDIR /app

# Gradleの定義ファイルをコピー
COPY --chown=demo:demogroup build.gradle.kts settings.gradle.kts ./

# 依存関係をキャッシュするために一度だけビルドを実行
RUN gradle build --no-daemon || return 0

# ソースコードをコピーしてアプリケーションをビルド
COPY --chown=demo:demogroup src ./src

# ここで実際にアプリケーションをビルド
RUN gradle bootJar --no-daemon

# --- 本番ステージ --------------------------------------------------
# 実行用のベースイメージ
FROM openjdk:17-jdk-alpine

# セキュリティのため、非rootユーザーを作成して切り替え
RUN addgroup -S demogroup && adduser -S demo -G demogroup
USER demo

# 作業ディレクトリを設定
WORKDIR /app

# ビルドステージからJARファイルのみをコピー
COPY --from=build --chown=demo:demogroup /app/build/libs/*.jar app.jar

# ポートを公開
EXPOSE 8080

# アプリケーションを実行
ENTRYPOINT ["java", "-jar", "app.jar"]