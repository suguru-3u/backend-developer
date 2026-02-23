plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    // メインクラス（mainメソッドがあるクラス）をフルパッケージ名で指定
    // 例: src/main/java/com/example/Main.java なら "com.example.Main"
    mainClass.set("org.example.Main")
}

dependencies {
    implementation("org.commonmark:commonmark:0.24.0")
}

tasks.test {
    useJUnitPlatform()
}