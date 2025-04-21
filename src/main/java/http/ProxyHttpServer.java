package http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyHttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Proxy 서버가 8080 포트에서 대기 중...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String line = in.readLine(); // 첫 줄만 읽음
            System.out.println("클라이언트 요청: " + line);

            String responseBody = "ERROR";
            try {
                responseBody = HttpProxyHelper.fetchExternalData();  // 외부 서버 호출
            } catch (Exception e) {
                e.printStackTrace();
                responseBody = "{\"error\":\"외부 서버 요청 실패\"}";
            }

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: application/json\r\n");
            out.write("\r\n");
            out.write(responseBody);
            out.flush();

            in.close();
            out.close();
            clientSocket.close();
        }
    }
}