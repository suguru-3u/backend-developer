package org.example.progrem1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ChildProcess {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in)
        );

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(System.out)
        );

        String message = reader.readLine();
        writer.write("Child received: " + message + "\n");
        writer.flush();
    }
}
