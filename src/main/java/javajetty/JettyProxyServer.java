package javajetty;

import com.google.gson.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

// Jetty 기반 프록시 서버 구현 예제
// - 클라이언트의 JSON POST 요청을 받아
// - 가공한 후 외부 서버에 POST 요청을 보내고
// - 응답을 클라이언트에게 그대로 반환함
//
// 📌 사용 예시 (curl):
// curl -X POST http://localhost:8080/proxy \
//   -H "Content-Type: application/json" \
//   -d '{"user":"kim"}'



public class JettyProxyServer {
    public static void main(String[] args) throws Exception {
        // Jetty 서버 인스턴스를 생성하고 포트 8080에서 실행
        Server server = new Server(8080);

        // 서블릿 핸들러를 등록하여 "/proxy" 경로로 요청을 처리하도록 설정
        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(new ProxyServlet()), "/proxy");

        server.setHandler(handler);
        server.start();  // 서버 시작
        server.join();   // 메인 쓰레드가 서버 종료까지 대기
    }

    // ✅ 클라이언트의 JSON 요청을 읽고 가공한 후 외부 서버로 전달하고 결과를 다시 반환
    public static class ProxyServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

            // 요청 바디(JSON) 읽기
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            // JSON 가공
            JsonObject input = JsonParser.parseString(sb.toString()).getAsJsonObject();
            JsonObject wrapped = new JsonObject();
            wrapped.addProperty("fromProxy", true);
            wrapped.add("original", input);

            // 외부 서버로 POST 요청 전송
            String externalResponse = sendToExternalServer(wrapped.toString());

            // 클라이언트에 응답 반환
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(externalResponse);
        }

        // 외부 서버로 POST 요청을 보내고 응답을 반환
        private String sendToExternalServer(String jsonBody) throws IOException {
            URL url = new URL("http://localhost:9000");  // 외부 서버 주소
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // 요청 본문 전송
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                out.write(jsonBody);
                out.flush();
            }

            // 응답 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) response.append(line);
            }

            return response.toString();
        }
    }
}
