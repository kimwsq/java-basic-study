package collection;

import java.util.*;

/*
    * 리스트 안에 맵을 저장하는 예제입니다.
    * 테이블 구조 데이터를 표현할 때 유용합니다. DB Row와 비슷한 구조로,
    * 각 행(row)을 맵으로 표현하고,
    * 그 맵들을 리스트에 저장하여 전체 테이블을 표현할 수 있습니다.
    * 예를 들어, 사용자 목록을 저장할 때 각 사용자의 정보를 맵으로 표현하고,
    * 그 맵들을 리스트에 저장하여 전체 사용자 목록을 표현할 수 있습니다.
    * 이 예제에서는 사용자 이름과 이메일을 저장하는 맵을 리스트에 추가하고,
    * 각 사용자의 정보를 출력합니다.
    * 리스트는 순서가 있는 데이터 구조로, 중복된 값을 허용합니다.
    * 맵은 키-값 쌍으로 데이터를 저장하는 데이터 구조로, 키는 중복을 허용하지 않습니다.
 */

public class ListOfMapSample {
    public static void main(String[] args) {
        List<Map<String, String>> users = new ArrayList<>();

        Map<String, String> user1 = new HashMap<>();
        user1.put("name", "Alice");
        user1.put("email", "alice@example.com");

        Map<String, String> user2 = new HashMap<>();
        user2.put("name", "Bob");
        user2.put("email", "bob@example.com");

        users.add(user1);
        users.add(user2);

        for (Map<String, String> user : users) {
            System.out.println("이름: " + user.get("name") + ", 이메일: " + user.get("email"));
        }
    }
}