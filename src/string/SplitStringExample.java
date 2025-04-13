package string;

public class SplitStringExample {
    public static void main(String[] args) {
        String input = "apple#banana#cherry";
        String[] fruits = input.split("#");

        for (String fruit : fruits) {
            System.out.println(fruit);
        }
    }
}