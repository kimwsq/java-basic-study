package jsonmulticlient;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/*
요청
{
  "name": "Alice",
  "task": "Deploy"
}
기대 출력
[응답] Alice → {"greeting":"Hello, Alice","taskReceived":"TestConnection","thread":"pool-1-thread-2"}
[응답] Bob → {"greeting":"Hello, Bob","taskReceived":"TestConnection","thread":"pool-1-thread-3"}
...

 */


public class JsonProxyServer {
    private static final int PORT = 8083;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("JSON 프록시 서버 시작: " + PORT);

        while (true) {
            Socket client = server.accept();
            pool.execute(() -> handle(client));
        }
    }

    private static void handle(Socket client) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))
        ) {
            // 헤더 읽기
            String line;
            int contentLength = 0;
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // 바디 읽기
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars);
            String body = new String(bodyChars);

            // JSON 파싱
            JsonObject requestJson = JsonParser.parseString(body).getAsJsonObject();
            String name = requestJson.get("name").getAsString();
            String task = requestJson.get("task").getAsString();

            // 응답 JSON 만들기
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("greeting", "Hello, " + name);
            responseJson.addProperty("taskReceived", task);
            responseJson.addProperty("thread", Thread.currentThread().getName());

            // 응답 전송
            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: application/json\r\n\r\n");
            out.write(responseJson.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
