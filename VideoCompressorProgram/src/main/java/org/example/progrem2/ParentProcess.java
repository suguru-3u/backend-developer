package org.example.progrem2;

import java.io.*;

public class ParentProcess {
    public static void main(String[] args) throws Exception {
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter("/tmp/myfifo"))) {

            writer.write("Hello from Parent\n");
            writer.flush();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}