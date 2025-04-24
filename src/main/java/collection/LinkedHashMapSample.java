package collection;

import java.util.*;
/*
    * LinkedHashMap은 HashMap과 TreeMap의 장점을 결합한 자료구조입니다.
    * 순서 보장 Map
    * JSON 객체와 유사한 구조로, 순서가 보장된 키-값 쌍을 저장합니다.
    * JSON Serialization과 Deserialization에 유용합니다.
 */

public class LinkedHashMapSample {
    public static void main(String[] args) {
        Map<String, String> orderedMap = new LinkedHashMap<>();
        orderedMap.put("first", "1");
        orderedMap.put("second", "2");
        orderedMap.put("third", "3");

        for (String key : orderedMap.keySet()) {
            System.out.println(key + " = " + orderedMap.get(key));
        }
    }
}
