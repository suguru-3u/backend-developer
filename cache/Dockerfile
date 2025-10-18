# ----------------------------------------------------------------------
# 1. ビルダー・ステージ: アプリケーションのコンパイルとビルド
# ----------------------------------------------------------------------
FROM gradle:jdk17-alpine AS builder

# ワークディレクトリを設定
WORKDIR /app

# Gradleの設定ファイルとソースコードをコピー
# COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts /app/
# COPY --chown=gradle:gradle src /app/src

# もしプロジェクト全体をコピーする場合は以下
COPY . /app

# アプリケーションのビルドを実行
# キャッシュを最適化するため、依存関係のダウンロードとビルドを分離することが推奨されますが、
# シンプルな例として一括で実行します。
RUN gradle clean build -x test

# ----------------------------------------------------------------------
# 2. 実行環境ステージ: 実行に必要なものだけを含む軽量イメージ
# ----------------------------------------------------------------------
FROM openjdk:17-jdk-slim

# ワークディレクトリを設定
WORKDIR /app

# ビルダー・ステージからビルドされたjarファイルをコピー
# プロジェクト名やビルド構成によってパスは適宜修正してください
COPY --from=builder /app/build/libs/*.jar app.jar

# 静的ファイルをコンテナ内の所定の場所にコピー
# Nginxの設定でSpring Bootの静的ファイルを直接配信しない場合でも、
# Spring Boot自体がHTMLやAPIを配信するために必要です
COPY src/main/resources/static/ /app/static/

# 実行環境のタイムゾーンを設定（任意）
ENV TZ Asia/Tokyo

# Spring Bootアプリケーションの起動
ENTRYPOINT ["java", "-jar", "app.jar"]

# Spring Bootのデフォルトポートを公開 (Docker内部のみ)
EXPOSE 8080