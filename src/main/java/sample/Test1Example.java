package sample;

/*
	input.txt라는 파일을 만든다.
	파일에 "apple#banana#cherry"를 저장한다.
	저장한 파일을 다시 읽어서, #로 구분하여 한 줄씩 출력한다.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Test1Example {

    public static void main(String[] args) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("input.txt"))) {
            writer.write("apple#banana#cherry");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try(BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while((line = reader.readLine()) !=null) {
                String[] arr = line.split("#");
                for(String s : arr) {
                    System.out.println(s);
                }

            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
