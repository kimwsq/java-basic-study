package thread;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class FastOrderProxy {
    private static final ExecutorService pool = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8081);
        System.out.println("[빠른 순서] 프록시 서버 시작");

        while (true) {
            Socket client = server.accept();
            pool.execute(() -> handle(client));
        }
    }

    private static void handle(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            in.readLine(); // GET / HTTP/1.1 무시
            while (!in.readLine().isEmpty()); // 헤더 무시

            String result = fetch("localhost", 9001); // 외부 서버 호출
            out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n");
            out.write(result);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fetch(String host, int port) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) result.append(line).append("\n");
            return result.toString();
        }
    }
}
