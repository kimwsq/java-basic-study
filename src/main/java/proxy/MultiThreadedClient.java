package proxy;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MultiThreadedClient {
    public static void main(String[] args) throws InterruptedException {
        int[] ports = {8001, 8002, 8001, 8002, 8001};
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < ports.length; i++) {
            int port = ports[i];
            int index = i;
            executor.submit(() -> {
                try {
                    URL url = new URL("http://localhost:8080/" + port);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    in.close();

                    System.out.println("[#" + index + "] 요청 → " + port + " | 응답 ↓↓↓");
                    System.out.println(response);
                } catch (Exception e) {
                    System.out.println("[#" + index + "] 요청 실패: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
