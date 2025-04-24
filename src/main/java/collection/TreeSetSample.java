package collection;

import java.util.*;

/*
  * 중복 없이 정렬
  * 정렬된 키워드 목록을 출력하는 예제입니다.

 */

public class TreeSetSample {
    public static void main(String[] args) {
        Set<String> sortedWords = new TreeSet<>();
        sortedWords.add("banana");
        sortedWords.add("apple");
        sortedWords.add("cherry");

        System.out.println("정렬된 단어 목록:");
        for (String word : sortedWords) {
            System.out.println(word);
        }
    }
}
