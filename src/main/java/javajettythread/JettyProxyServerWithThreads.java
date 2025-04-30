package javajettythread;

// Jetty 기반 프록시 서버 구현 예제
// - 클라이언트의 JSON POST 요청을 받아
// - 가공한 후 외부 서버로 POST 요청을 보내고
// - 그 응답을 다시 클라이언트에 전달함
//
// 두 가지 버전을 포함:
// 1. 비순차 응답 처리 방식 (요청 완료되는 순서대로 응답)
// 2. 순차 응답 보장 방식 (요청된 순서대로 응답)

import com.google.gson.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class JettyProxyServerWithThreads {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(new NonOrderedProxyServlet()), "/fast");
        handler.addServlet(new ServletHolder(new OrderedProxyServlet()), "/ordered");
        server.setHandler(handler);
        server.start();
        server.join();
    }

    // ✅ 버전 1: 비순차 응답 (빠른 응답부터 반환)
    public static class NonOrderedProxyServlet extends HttpServlet {
        private final ExecutorService executor = Executors.newFixedThreadPool(10);

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

            // 요청 바디를 문자열로 읽음
            String requestBody = new BufferedReader(req.getReader())
                    .lines().reduce("", (acc, cur) -> acc + cur);

            executor.submit(() -> {
                try {
                    // JSON 가공
                    JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
                    JsonObject wrapped = new JsonObject();
                    wrapped.addProperty("fromProxy", true);
                    wrapped.add("original", json);

                    // 외부 서버에 POST 요청 보내고 결과 받기
                    String externalResponse = sendToExternal(wrapped.toString());

                    // 클라이언트에게 직접 응답 (스레드에서 직접)
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(externalResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // ✅ 버전 2: 순차 응답 보장 (요청 순서대로 처리)
    public static class OrderedProxyServlet extends HttpServlet {
        private final ExecutorService executor = Executors.newFixedThreadPool(10);
        private final BlockingQueue<CompletableFuture<ResponseHolder>> queue = new LinkedBlockingQueue<>();

        public OrderedProxyServlet() {
            // 응답 소비자 스레드: queue 순서대로 응답 처리
            new Thread(() -> {
                while (true) {
                    try {
                        CompletableFuture<ResponseHolder> future = queue.take();
                        ResponseHolder holder = future.get();
                        HttpServletResponse resp = holder.response;
                        String body = holder.body;
                        resp.setContentType("application/json");
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(body);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

            String requestBody = new BufferedReader(req.getReader())
                    .lines().reduce("", (acc, cur) -> acc + cur);

            // 순서 보장용 future 객체 생성
            CompletableFuture<ResponseHolder> future = new CompletableFuture<>();
            queue.offer(future);

            executor.submit(() -> {
                try {
                    JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
                    JsonObject wrapped = new JsonObject();
                    wrapped.addProperty("fromProxy", true);
                    wrapped.add("original", json);

                    String externalResponse = sendToExternal(wrapped.toString());
                    future.complete(new ResponseHolder(resp, externalResponse));
                } catch (Exception e) {
                    future.complete(new ResponseHolder(resp, "{\"error\":\"" + e.getMessage() + "\"}"));
                }
            });
        }
    }

    // 외부 서버에 JSON POST 요청 보내고 결과 문자열로 반환
    private static String sendToExternal(String jsonBody) throws IOException {
        URL url = new URL("http://localhost:9000");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
            out.write(jsonBody);
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) result.append(line);
        }

        return result.toString();
    }

    // 응답 객체를 future에 담기 위한 구조체
    static class ResponseHolder {
        HttpServletResponse response;
        String body;

        public ResponseHolder(HttpServletResponse response, String body) {
            this.response = response;
            this.body = body;
        }
    }
}

