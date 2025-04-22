package proxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
요청 1회씩 순차 전송

[요청 → 8001] 응답 ↓↓↓
응답 from external server at port 8001

[요청 → 8002] 응답 ↓↓↓
응답 from external server at port 8002

[요청 → 8001] 응답 ↓↓↓
응답 from external server at port 8001

...


 */

public class SimpleProxyClient {
    public static void main(String[] args) {
        int[] portsToTest = {8001, 8002, 8001, 8002};

        for (int port : portsToTest) {
            try {
                URL url = new URL("http://localhost:8080/" + port);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line).append("\n");
                }
                in.close();

                System.out.println("[요청 → " + port + "] 응답 ↓↓↓");
                System.out.println(response);
            } catch (Exception e) {
                System.out.println("[요청 실패 → " + port + "] " + e.getMessage());
            }
        }
    }
}
