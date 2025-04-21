package gson;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

public class JsonParseExample {
    public static void main(String[] args) {
        String json = """
        {
          "user": {
            "id": 101,
            "name": "Alice",
            "email": "alice@example.com"
          },
          "tasks": [
            { "id": 1, "title": "Study Java", "completed": true },
            { "id": 2, "title": "Write Proxy Server", "completed": false },
            { "id": 3, "title": "Refactor Code", "completed": true }
          ]
        }
        """;

        // JSON 파싱
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        // user.name 추출
        String userName = root.getAsJsonObject("user").get("name").getAsString();

        // 완료된 task 제목만 추출
        JsonArray tasks = root.getAsJsonArray("tasks");
        List<String> completedTaskTitles = new ArrayList<>();

        for (JsonElement taskElement : tasks) {
            JsonObject task = taskElement.getAsJsonObject();
            if (task.get("completed").getAsBoolean()) {
                completedTaskTitles.add(task.get("title").getAsString());
            }
        }

        // 결과 출력
        JsonObject result = new JsonObject();
        result.addProperty("user", userName);
        result.add("completed_tasks", new Gson().toJsonTree(completedTaskTitles));

        System.out.println(result.toString());
    }
}