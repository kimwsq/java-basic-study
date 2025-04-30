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

public class JettyProxyServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(new ProxyServlet()), "/proxy");

        server.setHandler(handler);
        server.start();
        server.join();
    }

    public static class ProxyServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

            // JSON 읽기
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            // 가공
            JsonObject input = JsonParser.parseString(sb.toString()).getAsJsonObject();
            JsonObject wrapped = new JsonObject();
            wrapped.addProperty("fromProxy", true);
            wrapped.add("original", input);

            // 외부 서버로 POST 요청
            String externalResponse = sendToExternalServer(wrapped.toString());

            // 클라이언트에 응답
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(externalResponse);
        }

        private String sendToExternalServer(String jsonBody) throws IOException {
            URL url = new URL("http://localhost:9000");  // 외부 서버 주소
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                out.write(jsonBody);
                out.flush();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) response.append(line);
            }

            return response.toString();
        }
    }
}
