package jsonmulticlient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;

public class JsonProxyClient {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        String[] names = {"Alice", "Bob", "Charlie", "Dana", "Eve"};

        for (String name : names) {
            pool.submit(() -> sendRequest(name));
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void sendRequest(String name) {
        String json = String.format("""
        {
          "name": "%s",
          "task": "TestConnection"
        }
        """, name);

        try {
            URL url = new URL("http://localhost:8083/proxy");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            out.write(json);
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            System.out.println("[응답] " + name + " → " + response);
        } catch (Exception e) {
            System.out.println("[에러] " + name + ": " + e.getMessage());
        }
    }
}
