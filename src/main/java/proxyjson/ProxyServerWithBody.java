package proxyjson;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import com.google.gson.*;

public class ProxyServerWithBody {
    private static final int PORT = 8080;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("프록시 서버 (Request Body 기반) 시작: " + PORT);

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
            // HTTP 요청 헤더 읽기
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

            // JSON 파싱 → targetPort 추출
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            int targetPort = json.get("targetPort").getAsInt();

            // 외부 서버에 요청
            String responseFromExternal = fetchFromExternalServer(targetPort);

            // 응답 전송
            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: text/plain\r\n\r\n");
            out.write(responseFromExternal);
            out.flush();

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
