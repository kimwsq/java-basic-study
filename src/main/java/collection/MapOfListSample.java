package collection;

import java.util.*;

/*
    * Map<String, List<String>>를 사용하여 과목별 수강생 목록을 관리하는 예제입니다.
    * 각 과목에 대해 수강생 목록을 저장하고, 이를 출력합니다.
    * 그룹핑된 목록
    * 분류 -> 항목 리스트
    * 예: 과목 -> 수강생 목록
    * 과목: Java, 수강생 목록: [Alice, Bob]
    * 과목: Python, 수강생 목록: [Charlie, Dana]
 */

public class MapOfListSample {
    public static void main(String[] args) {
        Map<String, List<String>> courseStudents = new HashMap<>();

        courseStudents.put("Java", Arrays.asList("Alice", "Bob"));
        courseStudents.put("Python", Arrays.asList("Charlie", "Dana"));

        for (String course : courseStudents.keySet()) {
            System.out.println("[" + course + "] 수강생 목록:");
            for (String student : courseStudents.get(course)) {
                System.out.println(" - " + student);
            }
        }
    }
}
