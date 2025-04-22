package orderedproxy;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/*
1. 클라이언트 요청 → 대기열 등록 (BlockingQueue)
2. 각 요청은 별도 스레드에서 처리
3. 완료된 응답은 Future로 대기열에 저장
4. 메인 응답 소비 스레드가 순서대로 응답을 꺼내어 클라이언트에게 전송

그냥 JsonProxyClient 예제를 포트 8084에 보내면 돼:
URL url = new URL("http://localhost:8084/proxy");

응답은 처리 속도와 무관하게 항상 요청 순서대로 나와야 해:
[응답] Alice → {name: "Alice", ...}
[응답] Bob   → {name: "Bob", ...}
[응답] Charlie → {name: "Charlie", ...}

 */


public class OrderedJsonProxy {
    private static final int PORT = 8084;
    private static final ExecutorService workerPool = Executors.newFixedThreadPool(5);
    private static final BlockingQueue<ResponseJob> responseQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[순서 보장] JSON 프록시 서버 시작: " + PORT);

        // 응답 순서 소비 스레드
        new Thread(() -> {
            while (true) {
                try {
                    ResponseJob job = responseQueue.take();  // 순서 보장
                    String result = job.future.get();         // 결과 기다리기

                    // 클라이언트에게 응답 전송
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(job.client.getOutputStream()));
                    out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n");
                    out.write(result);
                    out.flush();
                    job.client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 요청 처리 loop
        while (true) {
            Socket client = serverSocket.accept();
            handleRequest(client);
        }
    }

    private static void handleRequest(Socket client) {
        CompletableFuture<String> future = new CompletableFuture<>();
        responseQueue.offer(new ResponseJob(future, client));

        workerPool.submit(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                int contentLength = 0;
                while (!(line = in.readLine()).isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }

                char[] body = new char[contentLength];
                in.read(body);
                String bodyStr = new String(body);

                JsonObject input = JsonParser.parseString(bodyStr).getAsJsonObject();
                String name = input.get("name").getAsString();
                String task = input.get("task").getAsString();

                // 응답 지연 시뮬레이션
                Thread.sleep((long) (500 + Math.random() * 1500));

                JsonObject response = new JsonObject();
                response.addProperty("name", name);
                response.addProperty("task", task);
                response.addProperty("thread", Thread.currentThread().getName());
                response.addProperty("status", "processed");

                future.complete(response.toString());

            } catch (Exception e) {
                future.complete("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    // 요청과 연결을 묶어 관리
    static class ResponseJob {
        CompletableFuture<String> future;
        Socket client;

        ResponseJob(CompletableFuture<String> future, Socket client) {
            this.future = future;
            this.client = client;
        }
    }
}