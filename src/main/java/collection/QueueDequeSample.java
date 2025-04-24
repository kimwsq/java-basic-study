package collection;

import java.util.*;

/*
    * Queue와 Deque는 Java Collections Framework에서 제공하는 자료구조입니다.
    * 선입선출 & 양방향
    * 실시간 데이터 처리, 캐시 시스템 등에서 유용
    * 처리 순서 큐, 브라우저 히스토리
 */

public class QueueDequeSample {
    public static void main(String[] args) {
        // Queue 예제
        Queue<String> queue = new LinkedList<>();
        queue.offer("A");
        queue.offer("B");
        queue.offer("C");

        System.out.println("Queue 선입선출:");
        while (!queue.isEmpty()) {
            System.out.println(queue.poll());
        }

        // Deque 예제
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("X");
        deque.addLast("Y");
        deque.addLast("Z");

        System.out.println("Deque 앞에서 꺼내기: " + deque.pollFirst());
        System.out.println("Deque 뒤에서 꺼내기: " + deque.pollLast());
    }
}