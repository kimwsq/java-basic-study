package util;

import java.util.*;

// 1. 정렬 대상 클래스: Person
class Person {
    private String name;
    private int age;

    // 생성자
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Getter 메서드
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    // 출력 편의를 위한 toString 오버라이드
    @Override
    public String toString() {
        return name + " (" + age + ")";
    }
}

// 2. Comparator 인터페이스 구현 클래스 (오름차순 기준)
class AgeComparator implements Comparator<Person> {
    @Override
    public int compare(Person p1, Person p2) {
        // 나이 오름차순: 나이가 작으면 앞으로
        return Integer.compare(p1.getAge(), p2.getAge());
    }
}

// 3. 역순 정렬을 위한 Comparator (선택 사항)
class AgeDescComparator implements Comparator<Person> {
    @Override
    public int compare(Person p1, Person p2) {
        // 나이 내림차순: 나이가 많을수록 앞으로
        return Integer.compare(p2.getAge(), p1.getAge());
    }
}

// 4. 실행 클래스
public class ComparatorExample {
    public static void main(String[] args) {
        // 사람 목록 생성
        List<Person> people = new ArrayList<>();
        people.add(new Person("Alice", 30));
        people.add(new Person("Bob", 20));
        people.add(new Person("Charlie", 25));

        // 정렬 전 출력
        System.out.println("정렬 전:");
        people.forEach(System.out::println);

        // 오름차순 정렬
        Collections.sort(people, new AgeComparator());

        System.out.println("\n나이 오름차순 정렬:");
        people.forEach(System.out::println);

        // 내림차순 정렬
        Collections.sort(people, new AgeDescComparator());

        System.out.println("\n나이 내림차순 정렬:");
        people.forEach(System.out::println);

        // 람다식 정렬
        people.sort((p1, p2) -> Integer.compare(p1.getAge(), p2.getAge()));
        System.out.println("\n람다식으로 오름차순 정렬:");
        people.forEach(System.out::println);

        // comparing() 정렬
        people.sort(Comparator.comparing(Person::getAge));
        System.out.println("\nComparator.comparing() 정렬:");
        people.forEach(System.out::println);

        // 다중 정렬 기준: 나이 -> 이름
        people.sort(Comparator.comparing(Person::getAge).thenComparing(Person::getName));
        System.out.println("\n나이 -> 이름 기준 다중 정렬:");
        people.forEach(System.out::println);
    }
}