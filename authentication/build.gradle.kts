plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.sample"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Opean Id Connectの実装に必要なライブラリ
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf") // ログイン後の画面用など
    // Jakarta Servlet API を追加
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    // Web サーバー関連の依存関係
    implementation("org.springframework.boot:spring-boot-starter-web")  // 追加

    // MyBatis
    implementation("org.springframework.boot:spring-boot-starter-jdbc") // MyBatisはspring-boot-starter-jdbc の上に乗っかる形
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3") // 最新安定版
    runtimeOnly("com.mysql:mysql-connector-j") // MySQLドライバ

    // JSON用
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0") // 最新バージョンに適宜置き換えてください

    // JWT用
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // JSONパーサ

    // Oauth
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.springframework.security:spring-security-oauth2-jose")

    // https://mvnrepository.com/artifact/redis.clients/jedis
    implementation("redis.clients:jedis:5.1.1")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
