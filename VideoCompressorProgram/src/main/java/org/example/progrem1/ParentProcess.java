package org.example.progrem1;

import java.io.*;

public class ParentProcess {
    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("user.dir"));
        System.out.println(System.getProperty("java.class.path"));

        String classpath = System.getProperty("java.class.path");

        String absoluteClasspath = new File(classpath)
                .getAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-cp",
                absoluteClasspath,
                "org.example.progrem1.ChildProcess"
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 子プロセスへメッセージ送信
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream())
        );

        writer.write("Hello from Parent\n");
        writer.flush();

        // 子プロセスから受信
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Received: " + line);
        }

        process.waitFor();
    }
}