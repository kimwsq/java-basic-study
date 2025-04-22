package jsondto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

// 출력 결과
//{
//        "user": "Jin",
//        "roles": ["admin", "editor"],
//        "completed_tasks": [
//          { "title": "Complete HTTP Proxy", "priority": "high" },
//          { "title": "Refactor Codebase", "priority": "low" }
//        ]
//}


public class JsonProcessingExample {
    public static void main(String[] args) {
        String json = """
        {
          "meta": {
            "version": "1.0",
            "timestamp": "2025-04-14T22:30:00Z"
          },
          "user": {
            "id": 501,
            "name": "Jin",
            "roles": ["admin", "editor"]
          },
          "tasks": [
            {
              "id": 100,
              "title": "Complete HTTP Proxy",
              "status": "done",
              "details": {
                "priority": "high",
                "due_date": "2025-04-15"
              }
            },
            {
              "id": 101,
              "title": "Write Gson DTO Mapper",
              "status": "in_progress",
              "details": {
                "priority": "medium",
                "due_date": "2025-04-17"
              }
            },
            {
              "id": 102,
              "title": "Refactor Codebase",
              "status": "done",
              "details": {
                "priority": "low",
                "due_date": "2025-04-20"
              }
            }
          ]
        }
        """;

        Gson gson = new Gson();

        // 전체 JSON 파싱
        Root root = gson.fromJson(json, Root.class);

        // 추출할 필드
        String userName = root.getUser().getName();
        List<String> roles = root.getUser().getRoles();

        // 완료된 작업 추출
        JsonArray completedTasksArray = new JsonArray();
        for (Task task : root.getTasks()) {
            if ("done".equals(task.getStatus())) {
                JsonObject taskObj = new JsonObject();
                taskObj.addProperty("title", task.getTitle());
                taskObj.addProperty("priority", task.getDetails().getPriority());
                completedTasksArray.add(taskObj);
            }
        }

        // 최종 응답 JSON 만들기
        JsonObject result = new JsonObject();
        result.addProperty("user", userName);
        result.add("roles", gson.toJsonTree(roles));
        result.add("completed_tasks", completedTasksArray);

        System.out.println(result.toString());
    }
}