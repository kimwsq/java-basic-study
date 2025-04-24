package collection;

import java.util.HashMap;
import java.util.Map;

/*
    * HashMap을 사용하여 전화번호부를 관리하는 예제입니다.
    * 전화번호부는 이름을 키로 하고 전화번호를 값으로 저장합니다.
    * 전화번호부는 중복된 이름을 허용하지 않으며, 키를 통해 값을 검색할 수 있습니다.
    * HashMap은 해시 테이블을 기반으로 하여 빠른 검색 속도를 제공합니다.
 */

public class MapSample {
    public static void main(String[] args) {
        Map<String, String> phoneBook = new HashMap<>();

        phoneBook.put("Alice", "010-1234-5678");
        phoneBook.put("Bob", "010-2222-3333");
        phoneBook.put("Alice", "010-9999-9999");  // 키 중복 시 값 덮어씀

        System.out.println("전화번호 목록:");
        for (String name : phoneBook.keySet()) {
            String number = phoneBook.get(name);
            System.out.println(name + " : " + number);
        }

        System.out.println("Bob의 번호: " + phoneBook.get("Bob"));
        System.out.println("등록된 인원 수: " + phoneBook.size());
    }
}
