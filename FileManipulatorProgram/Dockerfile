FROM ubuntu:24.04

# 対話型プロンプトの無効化
ENV DEBIAN_FRONTEND=noninteractive

# 必要なツールのインストール
RUN apt update && apt install -y \
    curl \
    unzip \
    openjdk-21-jdk \
    && apt clean && rm -rf /var/lib/apt/lists/*

# Gradleのインストール（バージョンは適宜変更してください）
ENV GRADLE_VERSION=8.7
RUN curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip \
    && unzip gradle.zip \
    && mv gradle-${GRADLE_VERSION} /opt/gradle \
    && rm gradle.zip

# パスを通す（これでどこでも gradle コマンドが使えます）
ENV PATH=$PATH:/opt/gradle/bin

WORKDIR /app

CMD ["bash"]