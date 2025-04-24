package gson;

import com.google.gson.*;
import java.io.*;

/*
{
  "meta": {
    "system": {
      "version": "1.2.0",
      "components": [
        {
          "name": "auth",
          "logs": [
            { "type": "login", "count": 120 },
            { "type": "logout", "count": 90 }
          ]
        },
        {
          "name": "payment",
          "logs": [
            { "type": "success", "count": 55 },
            { "type": "fail", "count": 8 }
          ]
        }
      ]
    }
  }
}
 */


public class ComplexJsonExample {
    public static void main(String[] args) {
        try {
            // JSON 파일 읽기
            BufferedReader reader = new BufferedReader(new FileReader("complex.json"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // 파싱
            JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
            JsonObject meta = root.getAsJsonObject("meta");
            JsonObject system = meta.getAsJsonObject("system");

            String version = system.get("version").getAsString();
            System.out.println("시스템 버전: " + version);

            JsonArray components = system.getAsJsonArray("components");
            for (JsonElement compElem : components) {
                JsonObject comp = compElem.getAsJsonObject();
                String name = comp.get("name").getAsString();
                System.out.println("▶ 컴포넌트: " + name);

                JsonArray logs = comp.getAsJsonArray("logs");
                for (JsonElement logElem : logs) {
                    JsonObject log = logElem.getAsJsonObject();
                    String type = log.get("type").getAsString();
                    int count = log.get("count").getAsInt();
                    System.out.println("    - 로그 타입: " + type + ", 횟수: " + count);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
