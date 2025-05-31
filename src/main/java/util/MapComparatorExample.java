package util;

import java.util.*;

public class MapComparatorExample {
    public static void main(String[] args) {
        // 정렬 대상 Map 생성
        Map<String, Integer> map = new HashMap<>();
        map.put("banana", 3);
        map.put("apple", 5);
        map.put("grape", 1);
        map.put("orange", 4);

        System.out.println("정렬 전:");
        map.forEach((k, v) -> System.out.println(k + " = " + v));

        // 1. 키 기준 오름차순 정렬
        Map<String, Integer> sortedByKey = new TreeMap<>(map);
        System.out.println("\n키 기준 오름차순 정렬:");
        sortedByKey.forEach((k, v) -> System.out.println(k + " = " + v));

        // 2. 값 기준 오름차순 정렬
        List<Map.Entry<String, Integer>> valueList = new ArrayList<>(map.entrySet());
        valueList.sort(Map.Entry.comparingByValue());

        System.out.println("\n값 기준 오름차순 정렬:");
        valueList.forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));

        // 3. 값 기준 내림차순 정렬
        valueList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        System.out.println("\n값 기준 내림차순 정렬:");
        valueList.forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));

        // 4. 키 기준 내림차순 정렬
        Map<String, Integer> sortedByKeyDesc = new TreeMap<>(Collections.reverseOrder());
        sortedByKeyDesc.putAll(map);
        System.out.println("\n키 기준 내림차순 정렬:");
        sortedByKeyDesc.forEach((k, v) -> System.out.println(k + " = " + v));
    }
}
