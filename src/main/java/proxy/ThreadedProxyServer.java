package proxy;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/*

http://localhost:8080/8001 → 응답 from port 8001
http://localhost:8080/8002 → 응답 from port 8002

 */


public class ThreadedProxyServer {
    private static final int PORT = 8080;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("프록시 서버가 " + PORT + " 포트에서 대기 중...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            threadPool.execute(() -> handleClient(clientSocket));
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            // 첫 줄: ex) GET /8001 HTTP/1.1
            String requestLine = in.readLine();
            System.out.println("클라이언트 요청: " + requestLine);

            if (requestLine == null || !requestLine.startsWith("GET")) return;

            // 요청 경로에서 포트 추출
            String[] parts = requestLine.split(" ");
            String path = parts[1];  // ex: /8001
            int targetPort = Integer.parseInt(path.substring(1));

            // 외부 서버에 요청
            String responseFromExternal = fetchFromExternalServer(targetPort);

            // 클라이언트에게 응답
            synchronized (out) {
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: text/plain\r\n");
                out.write("\r\n");
                out.write(responseFromExternal);
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fetchFromExternalServer(int port) {
        try (
                Socket socket = new Socket("localhost", port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        } catch (IOException e) {
            return "외부 서버 연결 실패 (port: " + port + ")";
        }
    }
}