package javajettythreadintegrated;

// Jetty 기반 프록시 서버 구현 예제
// - 클라이언트의 JSON POST 요청을 받아
// - 가공한 후 외부 서버 여러 곳에 POST 요청을 보내고
// - 각 응답을 모두 수집하여 하나의 JSON 배열로 클라이언트에 반환
/*
curl -X POST http://localhost:8080/aggregate \
  -H "Content-Type: application/json" \
  -d '{"user":"kim"}'

[
  { "status": "received", "source": "server9000" },
  { "status": "received", "source": "server9001" },
  { "status": "received", "source": "server9002" }
]

 */
import com.google.gson.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class JettyProxyServerWithThreads {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(new AggregatedProxyServlet()), "/aggregate");
        server.setHandler(handler);
        server.start();
        server.join();
    }

    // ✅ 외부 서버들로 병렬 요청 후 응답을 하나로 묶는 서블릿
    public static class AggregatedProxyServlet extends HttpServlet {
        private final ExecutorService executor = Executors.newFixedThreadPool(10);
        private final Gson gson = new Gson();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

            // 요청 바디 수신
            String requestBody = new BufferedReader(req.getReader())
                    .lines().reduce("", (acc, cur) -> acc + cur);

            JsonObject originalJson = JsonParser.parseString(requestBody).getAsJsonObject();
            JsonObject wrapped = new JsonObject();
            wrapped.addProperty("fromProxy", true);
            wrapped.add("original", originalJson);
            String payload = wrapped.toString();

            // 외부 서버 주소 목록 (다르게 지정 가능)
            List<String> urls = List.of(
                    "http://localhost:9000",
                    "http://localhost:9001",
                    "http://localhost:9002"
            );

            // 병렬 요청 생성
            List<CompletableFuture<JsonObject>> futures = new ArrayList<>();
            for (String url : urls) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return sendToExternal(url, payload);
                    } catch (IOException e) {
                        JsonObject error = new JsonObject();
                        error.addProperty("error", e.getMessage());
                        return error;
                    }
                }, executor));
            }

            // 모든 응답 수집
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            JsonArray aggregated = new JsonArray();
            for (CompletableFuture<JsonObject> future : futures) {
                try {
                    aggregated.add(future.get());
                } catch (Exception e) {
                    JsonObject error = new JsonObject();
                    error.addProperty("error", "Future failed: " + e.getMessage());
                    aggregated.add(error);
                }
            }

            // 최종 응답 반환
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(aggregated));
        }

        // 외부 서버에 POST 요청 전송
        private JsonObject sendToExternal(String urlStr, String jsonBody) throws IOException {
            URL url = new URL(urlStr);
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

            return JsonParser.parseString(result.toString()).getAsJsonObject();
        }
    }
}
