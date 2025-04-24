package collection;

import java.util.HashSet;
import java.util.Set;

public class SetSample {
    public static void main(String[] args) {
        Set<String> tags = new HashSet<>();

        tags.add("java");
        tags.add("backend");
        tags.add("java");  // 중복 무시됨

        System.out.println("등록된 태그:");
        for (String tag : tags) {
            System.out.println("- " + tag);
        }

        System.out.println("java 포함 여부: " + tags.contains("java"));
        System.out.println("전체 태그 수: " + tags.size());
    }
}