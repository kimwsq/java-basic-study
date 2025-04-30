package javaserversample;

/*
ExternalServer 실행 (9000 포트)

ProxyServer 실행 (8080 포트)

Postman 또는 curl로 요청 보내기
curl -X POST http://localhost:8080/proxy \
  -H "Content-Type: application/json" \
  -d '{"user":"kim", "action":"submit"}'
 */

// ProxyServer.java
import com.google.gson.*;
import java.io.*;
import java.net.*;

public class ProxyServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("프록시 서버(8080번 포트) 실행 중...");

        while (true) {
            Socket clientSocket = server.accept();
            new Thread(() -> handle(clientSocket)).start();
        }
    }

    private static void handle(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
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
            char[] buffer = new char[contentLength];
            in.read(buffer);
            String requestBody = new String(buffer);

            // JSON 파싱 및 가공
            JsonObject inputJson = JsonParser.parseString(requestBody).getAsJsonObject();
            JsonObject newJson = new JsonObject();
            newJson.addProperty("fromProxy", true);
            newJson.add("original", inputJson);

            // 외부 서버로 전송
            String externalResponse = postToExternalServer(newJson.toString());

            // 클라이언트에 응답 전송
            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: application/json\r\n\r\n");
            out.write(externalResponse);
            out.flush();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String postToExternalServer(String jsonPayload) throws IOException {
        Socket socket = new Socket("localhost", 9000);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.write("POST / HTTP/1.1\r\n");
        out.write("Host: localhost\r\n");
        out.write("Content-Type: application/json\r\n");
        out.write("Content-Length: " + jsonPayload.length() + "\r\n");
        out.write("\r\n");
        out.write(jsonPayload);
        out.flush();

        // 응답 읽기
        StringBuilder sb = new StringBuilder();
        String line;
        boolean bodyStart = false;
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                bodyStart = true;
                continue;
            }
            if (bodyStart) sb.append(line);
        }

        socket.close();
        return sb.toString();
    }
}
