package proxyjson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/*
클라이언트 요청
POST /proxy HTTP/1.1
Content-Type: application/json

{
  "targetPort": 8001
}

 */

public class ProxyJsonClient {
    public static void main(String[] args) throws Exception {
        String jsonBody = """
        {
            "targetPort": 8001
        }
        """;

        URL url = new URL("http://localhost:8080/proxy");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
            out.write(jsonBody);
            out.flush();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        System.out.println("응답 ↓↓↓");
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        in.close();
    }
}
