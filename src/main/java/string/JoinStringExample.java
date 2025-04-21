package string;

public class JoinStringExample {
    public static void main(String[] args) {
        String[] fruits = {"apple", "banana", "cherry"};
        String result = String.join("|", fruits);
        System.out.println(result);  // apple|banana|cherry
    }
}