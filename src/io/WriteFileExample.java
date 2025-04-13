package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFileExample {
    public static void main(String[] args){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("test.txt"))) {
            writer.write("hello#world#java");
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}