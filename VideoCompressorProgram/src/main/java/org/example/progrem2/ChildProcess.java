package org.example.progrem2;

import java.io.*;

public class ChildProcess {
    public static void main(String[] args) throws Exception {
        try (BufferedReader reader =
                     new BufferedReader(new FileReader("/tmp/myfifo"))) {

            String message = reader.readLine();
            System.out.println("Received: " + message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
