package thread;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class OrderedProxy {
    private static final ExecutorService workerPool = Executors.newFixedThreadPool(5);
    private static final BlockingQueue<CompletableFuture<String>> responseQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8082);
        System.out.println("[순서 보장] 프록시 서버 시작");

        // 응답 소비 스레드
        new Thread(() -> {
            while (true) {
                try {
                    CompletableFuture<String> future = responseQueue.take();
                    String response = future.get();  // 순서대로 대기
                    future.complete(response);       // 응답 전달
                } catch (Exception ignored) {}
            }
        }).start();

        while (true) {
            Socket client = serverSocket.accept();
            handle(client);
        }
    }

    private static void handle(Socket client) {
        // 요청 하나에 대해 future 만들기
        CompletableFuture<String> future = new CompletableFuture<>();
        responseQueue.offer(future);

        // 백그라운드에서 외부서버 요청 실행
        workerPool.submit(() -> {
            String result = fetch("localhost", 9001); // 외부 요청
            try {
                // 기다리고 있는 future에 응답 채우기
                future.complete(result);

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n");
                out.write(result);
                out.flush();
                client.close();
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
    }

    private static String fetch(String host, int port) {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) result.append(line).append("\n");
            return result.toString();
        } catch (IOException e) {
            return "외부 서버 응답 실패: " + e.getMessage();
        }
    }
}