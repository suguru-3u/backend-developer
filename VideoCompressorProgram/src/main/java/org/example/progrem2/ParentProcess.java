package org.example.progrem2;

import java.io.*;
import java.util.Scanner;

public class ParentProcess {
    public static void main(String[] args) throws Exception {
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter("/tmp/myfifo"))) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("なんか入力してください");

            writer.write(scanner.next());
            writer.flush();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}