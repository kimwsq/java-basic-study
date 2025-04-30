package javaserversample;

/*
[Client] --POST(JSON)--> [내 서버] --POST(JSON)--> [외부 서버]
                              |                             |
                        가공(변환)                  응답 다시 받음
                              \----------------------> [Client 응답]

         예제 외부 서버 (외부 서버 역할만 테스트용으로 사용)

 */

// ExternalServer.java
import java.io.*;
import java.net.*;

public class ExternalServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(9000);
        System.out.println("외부 서버(9000번 포트) 대기 중...");

        while (true) {
            Socket socket = server.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 요청 바디 읽기
            String line;
            int contentLength = 0;
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            char[] buffer = new char[contentLength];
            in.read(buffer);
            String requestBody = new String(buffer);

            // 응답 JSON 생성
            String responseJson = "{\"status\":\"received\",\"original\":" + requestBody + "}";

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: application/json\r\n\r\n");
            out.write(responseJson);
            out.flush();
            socket.close();
        }
    }
}

