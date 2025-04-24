package collection;

import java.util.ArrayList;
import java.util.List;

public class ListSample {
    public static void main(String[] args) {
        List<String> fruits = new ArrayList<>();

        fruits.add("apple");
        fruits.add("banana");
        fruits.add("apple");  // 중복 허용

        System.out.println("전체 과일 목록:");
        for (String fruit : fruits) {
            System.out.println("- " + fruit);
        }

        System.out.println("첫 번째 과일: " + fruits.get(0));
        System.out.println("전체 개수: " + fruits.size());
    }
}
