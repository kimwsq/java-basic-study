

## 1. 파일 읽기

### 1-1. TXT 한줄씩 (L1/L2)
```java
import java.nio.file.Files;
import java.nio.file.Paths;

Map<String, State> map = new HashMap<>();
for (String line : Files.readAllLines(Paths.get("STATE.TXT"))) {
    String[] el = line.split("#");
    // el[0]=name, el[1]=type, el[2]=url, el[3]=파라미터(콤마구분)
    // split("#") 결과 — 끝이 #으로 끝나면 el.length가 3일 수 있음
    // 예: "fetch#action#http://.../fetch#" → el.length == 3 (el[3] 없음!)
    // 반드시 el.length > 3 && !el[3].isEmpty() 체크 후 접근할 것

    State s = new State();
    s.name = el[0];
    s.type = el[1];
    s.url  = el[2];
    if (el.length > 3 && !el[3].isEmpty()) {
        s.keys = el[3].split(",");   // L2-변수용: 파라미터 키 배열 (el[3]이 "id,data" 형태일 때)
        // 만약 el[3]~el[끝]이 각각 옵션이면: s.options = Arrays.copyOfRange(el, 3, el.length);
    }
    map.put(s.name, s);
}
```

### 1-2. JSON 통째로 String (L3/L4)
```java
String json = new String(Files.readAllBytes(Paths.get("STATE.JSON")));
// 파일명 대소문자 주의: STATE.JSON, VARIABLE.JSON, WORKFLOW.JSON
// 시험지에 적힌 파일명을 그대로 사용 — "Workflow.JSON" vs "WORKFLOW.JSON" 다를 수 있음
```

### 1-3. BufferedReader (대안)
```java
BufferedReader br = new BufferedReader(new FileReader("DATA.TXT"));
String line;
while ((line = br.readLine()) != null) {
    line = line.trim();
    if (line.isEmpty()) continue;
    // 처리
}
br.close();
```

---

## 2. 콘솔 입출력 (L1/L2)

### 2-1. Scanner 입력 (솔루션 방식)
```java
Scanner scanner = new Scanner(System.in);
while (true) {                      // 종료 조건 없이 무한 반복 필수!
    String input = scanner.nextLine();
    State s = map.get(input);       // 1-1에서 만든 map에서 꺼냄
    // 출력 포맷은 시험지에 따라 다름 — 예: "타입 URL"
    System.out.println(s.type + " " + s.url);
}
// while(true)를 빼면 한 번만 실행되고 종료 → MOCK/채점기가 다음 입력 못 보냄 → 0점
// map.get()이 null이면 NPE — 입력값이 map에 없는 경우는 시험에서 안 나옴
```

### 2-2. L1-콘솔 MainEntry 뼈대 (1-1 + 2-1 조합)
```java
import java.nio.file.*;
import java.util.*;

public class MainEntry {
    public static void main(String[] args) throws Exception {
        // 1) 파일 읽기 → Map에 VO 저장 (1-1)
        Map<String, State> map = new HashMap<>();
        for (String line : Files.readAllLines(Paths.get("DATA/STATE.TXT"))) {
            String[] el = line.split("#");
            State s = new State();
            s.name = el[0];
            s.type = el[1];
            s.url  = el[2];
            map.put(s.name, s);
        }

        // 2) 콘솔 입력 → 조회 → 출력 (2-1)
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            State s = map.get(input);
            System.out.println(s.type + " " + s.url);
        }
    }
}
// 클래스명은 시험지에 지정된 이름 사용 — MainEntry가 아닐 수 있음
// throws Exception 필수 — Files.readAllLines가 IOException 발생
// 폴더 경로는 시험지 확인 — "DATA/STATE.TXT" or "STATE.TXT"
```

### 2-3. L2-변수 MainEntry 뼈대 (1-1 + 4-5a + 5-1 조합)
```java
import java.nio.file.*;
import java.util.*;
import java.net.URLEncoder;

public class MainEntry {
    public static void main(String[] args) throws Exception {
        // 1) Variable 로드 — 반드시 먼저!
        VariableManager.load();

        // 2) State 파일 읽기 → Map (1-1)
        Map<String, State> map = new HashMap<>();
        for (String line : Files.readAllLines(Paths.get("DATA/STATE.TXT"))) {
            String[] el = line.split("#");
            State s = new State();
            s.name = el[0];
            s.type = el[1];
            s.url  = el[2];
            if (el.length > 3 && !el[3].isEmpty()) {
                s.keys = el[3].split(",");
            }
            map.put(s.name, s);
        }

        // 3) 콘솔 입력 → Query String 조립 → 출력 (5-1)
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            State s = map.get(input);
            String print = s.type + " " + s.url;
            if (s.keys != null) {
                for (int i = 0; i < s.keys.length; i++) {
                    print += (i == 0) ? "?" : "&";
                    print += s.keys[i] + "=" + URLEncoder.encode(VariableManager.get(s.keys[i]), "UTF-8");
                }
            }
            System.out.println(print);
        }
    }
}
// L1-콘솔에서 복사 후 추가: VariableManager.load() + keys 파싱 + Query String 조립
// public 필드 방식 → getter 불필요, 생성자 안 만들기!
```

### 2-4. BufferedReader 입력 (대안)
```java
BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
String input = stdin.readLine().trim();
```

---

## 3. JSON 처리 (Gson)

### 3-0. JSON 형태 → 변환법 선택 가이드 (먼저 이거 보고 결정!)
```
┌─ JSON이 이렇게 생겼으면 ────────────────┬─ 이 방법을 써라 ──────────┐
│ {"state":{...}, "type":"...", 고정 구조 │ 3-1 DTO 클래스 (fromJson) │
│   → key 이름을 미리 아는 구조           │   key=필드명으로 매핑       │
├─────────────────────────────────────────┼───────────────────────────┤
│ {"keyword":"폰","id":"100a"}            │ 3-2 TypeToken             │
│   → key가 가변/임의, 값이 전부 같은 타입 │   Map<String,String>으로   │
├─────────────────────────────────────────┼───────────────────────────┤
│ {"key":"eH7bDVXX"} 응답 한두 개 꺼내기  │ 3-3 JsonObject 직접 get   │
│   → DTO 만들기 귀찮은 단발성            │   obj.get("key")          │
├─────────────────────────────────────────┼───────────────────────────┤
│ {"a":"1","b":"2",...} key 모름, 전부 순회│ 3-4 keySet/entrySet 순회 │
├─────────────────────────────────────────┼───────────────────────────┤
│ {"items":["x","y","z"]} 또는 [ {...} ]  │ 3-5 JsonArray 순회        │
├─────────────────────────────────────────┼───────────────────────────┤
│ 내가 응답 JSON을 만들어서 내보내야 함    │ 3-6 JsonObject 생성       │
└─────────────────────────────────────────┴───────────────────────────┘
요약: 구조 고정+중첩 → DTO / key 가변 동일타입 → TypeToken / 단발 꺼내기 → get
```

### 3-1. DTO 클래스로 역직렬화 (L3/L4 핵심!)
```java
import com.google.gson.Gson;

// ── 이런 JSON일 때 (구조가 고정, key 이름을 미리 앎, 중첩 객체) ──
// {
// "state": {
// "create": { "type":"action", "url":"http://...", "parameters":["id"] },
// "fetch":  { "type":"action", "url":"http://...", "parameters":[] }
// }
// }
// JSON 구조에 맞는 DTO 정의 — 필드명이 JSON key와 반드시 일치해야 함!
// "state" 키 → public ... state;  오타나면 null로 파싱되어 NPE 발생
class StatesDto {
    public Map<String, StateDto> state;   // JSON의 "state" 키 (안쪽 key가 가변이라 Map)
    static class StateDto {
        public String type;               // JSON의 "type" 키
        public String url;
        public List<String> parameters;   // JSON의 "parameters" 키 — 빈 배열 []도 정상 파싱됨
    }
}

// 한줄로 파싱 — 내부 클래스에서 사용 시 static class로 선언해야 Gson이 인스턴스 생성 가능
StatesDto dto = new Gson().fromJson(jsonString, StatesDto.class);
// 접근: dto.state.get("create").url
```

### 3-2. TypeToken으로 Map/List 역직렬화 (지난번 여기서 막힘!)
```java
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

// 학습노트 (지난번 시험 실패 원인):
// Gson 파싱할 때 "Type type = new TypeToken<...>(){}.getType()" 이게 필요한 줄 몰라서 막혔음.
// → Map/List 처럼 꺽쇠<>가 최상위면 .class로는 절대 안 되고, 반드시 이 Type 방식.
// → 다음엔: JSON이 통째로 Map/List면 TypeToken, 객체 하나면 DTO.class 또는 JsonObject.class.

// 핵심 규칙 
// 받을 타입의 "최상위에 꺽쇠 <> 가 있으면" → 무조건 이 Type 방식.
// Map<String,String>, List<String>, List<Dto>  →  TypeToken 필수
// 그냥 클래스(MyDto.class, JsonObject.class)     →  .class 사용 (3-1, 3-3)
//
// Map.class / List.class 로 받으면 컴파일되거나 되어도 값이 깨짐(숫자→Double 등) → 지난번 실패 원인
// Map<String,String>.class 는 문법상 작성 자체가 안 됨 → 그래서 Type이 필요한 것

// ── 이런 JSON일 때 (최상위가 통째로 Map, key가 가변/임의, 값 타입 동일) ──
// {
// "keyword": "스마트폰",
// "id":      "100a",
// "token":   "eH7bDVXX"
// }
Type type = new TypeToken<Map<String, String>>() {}.getType();  // (){} 빈 중괄호 필수! (없으면 타입 못 잡음)
Map<String, String> map = new Gson().fromJson(jsonString, type);
// 접근: map.get("keyword") → "스마트폰"

// ── 최상위가 배열일 때: [ {"id":"1"}, {"id":"2"} ] ──
Type listType = new TypeToken<List<MyDto>>() {}.getType();
List<MyDto> list = new Gson().fromJson(jsonString, listType);
```
>  **TypeToken 쓰기 싫으면 → DTO 클래스로 감싸라.**
> 제네릭이 "클래스의 필드 안"에 있으면 `.class`로 충분하다 (필드는 reflection으로 타입이 유지됨).
> 그래서 3-1의 `StatesDto`(필드가 `Map<String,StateDto> state`)는 `StatesDto.class`로 잘 됨.
> **정리: 최상위가 통째로 Map/List → TypeToken / DTO로 감싸면 → .class**
> (값이 숫자/객체가 섞여 있으면 `Map<String,Object>` 또는 3-3 JsonObject 방식으로)

### 3-3. JsonParser / JsonObject로 직접 꺼내기 (단발성)
```java
import com.google.gson.*;

// ── 이런 JSON일 때 (값 한두 개만 빠르게 꺼낼 때, DTO 만들기 아까움) ──
// {"name":"create", "count":3, "key":"eH7bDVXX"}
JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
// (= new Gson().fromJson(jsonString, JsonObject.class) 와 동일)
String val   = obj.get("name").getAsString();    // "create"
int num      = obj.get("count").getAsInt();      // 3   (문자열이면 getAsString)
if (obj.has("key")) { /* 필드 존재 체크 — 없는 key를 get하면 NPE */ }
// 중첩이면: obj.getAsJsonObject("state").get("type").getAsString()
```

### 3-4. JSON Object 순회 (key를 모를 때 전부)
```java
// ── 이런 JSON일 때 (응답의 모든 key/value를 훑어야 함, key가 미정) ──
// {"id":"100a", "token":"eH7b", "total":"1"}
for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
    String key = e.getKey();                      // "id", "token", "total"
    String value = e.getValue().getAsString();    // "100a" ...
    // 예: VariableManager.put(key, value);  ← 응답 전체를 변수로 반영 (3-7)
}
// 간단히 key만: for (String k : obj.keySet()) { obj.get(k).getAsString(); }
```

### 3-5. JSON Array 순회
```java
// ── 이런 JSON일 때 (배열 필드, 또는 최상위가 배열) ──
// 객체 안 배열: {"items": ["사과", "바나나", "체리"]}
JsonArray arr = obj.getAsJsonArray("items");
for (JsonElement e : arr) {
    String item = e.getAsString();                // "사과" ...
}

// 최상위가 통째로 배열: [{"id":"1"}, {"id":"2"}]
// JsonArray top = JsonParser.parseString(jsonString).getAsJsonArray();
// for (JsonElement e : top) { e.getAsJsonObject().get("id").getAsString(); }
// → DTO로 받으려면: new Gson().fromJson(json, new TypeToken<List<Dto>>(){}.getType())
```

### 3-6. JSON 응답 생성
```java
JsonObject result = new JsonObject();
result.addProperty("id", "100a");
result.addProperty("total", "1");
// result.toString() → {"id":"100a","total":"1"}
```

### 3-7. HTTP 응답 파싱 → 변수 업데이트 (L3/L4 필수 패턴)
```java
// Microservice 응답 예: {"key":"eH7bDVXX"} → key 변수를 "eH7bDVXX"로 업데이트
// 이 업데이트를 빠뜨리면 다음 State에서 이전 값을 사용 → 채점 실패
// 응답이 {} (빈 객체)인 경우에도 에러 나지 않음 — keySet()이 비어있을 뿐
JsonObject resp = new Gson().fromJson(responseBody, JsonObject.class);
for (String k : resp.keySet()) {
    VariableManager.put(k, resp.get(k).getAsString());
}
```

---

## 4. VO / DTO 클래스 패턴

### 4-1. 단순 VO (L1-콘솔) — public 필드 방식 (빠름)
```java
public class State {
    public String name;
    public String type;
    public String url;
    // 기본 생성자 자동 생성됨 → new State()로 생성 후 필드 직접 대입
}

// 사용:
State s = new State();
s.name = el[0];
s.type = el[1];
s.url  = el[2];
```

### 4-1b. 단순 VO — 생성자 방식 (정석)
```java
public class State {
    private String name;
    private String type;
    private String url;

    public State(String name, String type, String url) {
        this.name = name; this.type = type; this.url = url;
    }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getUrl()  { return url; }
}

// 사용:
State s = new State(el[0], el[1], el[2]);
// 생성자 정의하면 기본 생성자 사라짐 → new State() 불가!
// private 필드 → s.name 직접 접근 불가, getter 사용: s.getName()
```

### 4-2. VO에 필드 추가 (L2-변수 — keys 추가)
```java
private String[] keys;  // 파라미터 목록, null 가능

public State(String name, String type, String url, String[] keys) { ... }
public String[] getKeys() { return keys; }
```

### 4-3. State.java — abstract 기반 클래스 (L3/L4, 그대로 복붙)
```java
public abstract class State {
    private String name;
    private String next;

    public State(String name, String next) {
        this.name = name;
        this.next = next;
    }
    public String getName() { return name; }
    public String getNext() { return next; }
    public abstract String run() throws Exception;
}
// 이 파일은 시험에서 수정할 부분 없음 — 그대로 복붙
// ActionState, ParallelState, ChoiceState가 이걸 extends 함
```

### 4-4. Inner DTO → 8-5 WorkflowManager.java에 통합됨 (그쪽 참고)

### 4-5a. VariableManager — TXT 버전 (L2-변수)
```java
public class VariableManager {
    private static Map<String, String> variables = new HashMap<>();

    public static String get(String key) { return variables.get(key); }

    public static void load() throws Exception {
        for (String line : Files.readAllLines(Paths.get("DATA/VARIABLE.TXT"))) {
            String[] el = line.split("#");
            variables.put(el[0], el[1]);  // "keyword#스마트폰" → put("keyword", "스마트폰")
        }
    }
}
// 사용: main에서 VariableManager.load(); 한 번 호출 후 VariableManager.get("keyword")
// L2-변수는 싱글스레드 → synchronized 불필요
```

### 4-5b. VariableManager.java — JSON 버전 (L3/L4, 그대로 복붙)
```java
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public class VariableManager {
    private static Map<String, String> variables;

    public synchronized static String get(String key) { return variables.get(key); }
    public synchronized static String put(String key, String value) { return variables.put(key, value); }

    public static void load() throws Exception {
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        String json = new String(Files.readAllBytes(Paths.get("VARIABLE.JSON"))); /* 파일명 확인 */
        variables = Collections.synchronizedMap(new Gson().fromJson(json, type));
    }
}
// 이 파일은 파일명 외 수정할 부분 없음 — 그대로 복붙
```

---

## 5. Query String 조립

### 5-1. 기본 패턴 (L2-변수 콘솔 출력용)
```java
import java.net.URLEncoder;

String print = s.type + " " + s.url;
if (s.keys != null) {
    for (int i = 0; i < s.keys.length; i++) {
        print += (i == 0) ? "?" : "&";   // 첫 파라미터 앞은 ?, 나머지는 &
        print += s.keys[i] + "=" + URLEncoder.encode(VariableManager.get(s.keys[i]), "UTF-8");
    }
}
System.out.println(print);
// 파라미터가 없는 State → keys가 null → ?도 &도 안 붙여야 함
// fetch#action#http://.../fetch# → "action http://.../fetch" (뒤에 ? 붙으면 오답)
// URLEncoder: 한글/공백 등 특수문자 인코딩 — L2-변수에서도 필요할 수 있음
```

### 5-2. URLEncoder 포함 (L3/L4 HTTP 호출용)
```java
import java.net.URLEncoder;

// parameters가 빈 List인 경우(size==0) → 루프 안 돌아서 query=""
// 이때 fullUrl = baseUrl 그대로 → 정상 동작
StringBuilder query = new StringBuilder();
for (int i = 0; i < parameters.size(); i++) {
    query.append(i == 0 ? "?" : "&");
    String key = parameters.get(i);
    query.append(URLEncoder.encode(key, "UTF-8"))
         .append("=")
         .append(URLEncoder.encode(VariableManager.get(key), "UTF-8"));
    // VariableManager.get(key)가 null이면 NPE 발생
    // → Variable 파일에 해당 key가 있는지, 이전 State에서 업데이트되었는지 확인
}
String fullUrl = baseUrl + query.toString();
```

---

## 6. Jetty 12.1 HTTP Server (2026년!)

> 2026년은 Jetty 12.1! import가 완전히 다름. 3가지 방식 중 택1.

### 6-1. L4-워크플로우 EngineServer.java (JSON Body 응답, 그대로 복붙)
```java
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.http.HttpHeader;
import com.google.gson.*;

public class EngineServer {
    public static void start() throws Exception {
        Server server = new Server(8080);                         /* 포트 확인 */
        server.setHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response,
                    Callback callback) throws Exception {

                String name = request.getHttpURI().getPath().substring(1); /* path 파싱 방식 확인 */
                String result = WorkflowManager.get(name).run().toString();

                response.setStatus(200);
                response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json");
                Content.Sink.write(response, true, result, callback);
                return true;
            }
        });
        server.start();
        server.join();
    }
}
// 포트, path 파싱만 시험지 보고 확인 — 나머지 그대로 복붙
```

### 6-2. L3-HTTP EngineServer.java (빈 200 응답, 그대로 복붙)
```java
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.io.Content;
import com.google.gson.*;

public class EngineServer {
    public static void start() throws Exception {
        Server server = new Server(8080);                         /* 포트 확인 */
        server.setHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response,
                    Callback callback) throws Exception {

                // POST body에서 state 이름 꺼내기
                String body = Content.Source.asString(request);
                JsonObject obj = new Gson().fromJson(body, JsonObject.class);
                String name = obj.get("name").getAsString();      /* JSON key 확인 */

                StateManager.get(name).run();

                response.setStatus(200);
                callback.succeeded();
                return true;
            }
        });
        server.start();
        server.join();
    }
}
// 포트, body의 JSON key만 시험지 보고 확인 — 나머지 그대로 복붙
```

### 6-3. 방식A: 빈 200 응답 (snippet)
```java
response.setStatus(200);
callback.succeeded();     // callback 호출 안 하면 클라이언트가 응답을 못 받고 타임아웃
return true;              // false 리턴하면 Jetty가 요청을 처리 안 한 것으로 판단
```

### 6-4. 방식A: 요청 Body 읽기 (POST용)
```java
String body = Content.Source.asString(request);
```

### 6-5. 방식B: Servlet (Jetty 12 + jakarta.servlet)
```java
// Jetty 12 Servlet은 ee10 패키지 사용 — ee8(javax)과 ee10(jakarta) 혼용 불가
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;

Server server = new Server(8080);
ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
context.setContextPath("/");
context.addServlet(new ServletHolder(new EngineServlet()), "/*");
server.setHandler(context);
server.start();
server.join();   // join() 빠뜨리면 main 종료 → 서버도 종료 → MOCK.EXE 연결 실패
```
```java
// javax.servlet으로 import하면 컴파일은 되지만 Jetty 12 런타임에서 클래스 못 찾음
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class EngineServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String path = request.getRequestURI();
        String name = path.substring(path.lastIndexOf("/") + 1);

        response.setContentType("application/json");
        response.getWriter().write(result);
    }
}
```

### 6-6. JSON 받고 JSON 응답하는 범용 서버 (엔진 없이 — 복붙 후 switch만 채움)
```java
// 워크플로우 엔진(6-1/6-2)과 무관한 "그냥 JSON in → JSON out" 서버.
// readJson()/writeJson() 헬퍼까지 통째로 복붙 → switch 안의 로직만 채우면 끝.
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.http.HttpHeader;
import com.google.gson.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonServer {
    // Jetty는 요청마다 다른 스레드 → 공유 저장소는 반드시 ConcurrentHashMap
    private static Map<String, JsonObject> store = new ConcurrentHashMap<>();

    public static void start() throws Exception {
        Server server = new Server(8080);                          /* 포트 */
        server.setHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response,
                    Callback callback) throws Exception {

                String path   = request.getHttpURI().getPath();    // 예: "/monitoring"
                String method = request.getMethod();               // "GET" / "POST"
                JsonObject in = readJson(request);                 // 요청 body(JSON) → JsonObject
                JsonObject out = new JsonObject();                 // 응답으로 돌려줄 JSON

                switch (path) {                                    /* 경로별 로직만 채우기 */
                    case "/monitoring": {
                        // 예) 받은 데이터를 키로 저장
                        String key = in.get("agentId").getAsString();   // 값 끝 공백 주의 → .trim()
                        store.put(key, in);
                        out.addProperty("ok", 1);
                        break;
                    }
                    case "/query": {
                        // 예) GET 쿼리파라미터 읽기: /query?id=100a
                        String id = valueOf(request, "id");
                        out.addProperty("count", store.size());
                        break;
                    }
                    default:
                        response.setStatus(404);                   // 모르는 경로
                        callback.succeeded();
                        return true;
                }
                writeJson(response, callback, out);                // JsonObject → 200 JSON 응답
                return true;
            }
        });
        server.start();
        server.join();                                            // 종료 없이 대기 (필수!)
    }

    // ── 요청 body(JSON) → JsonObject. body 없으면(GET 등) 빈 객체 ──
    private static JsonObject readJson(Request request) throws Exception {
        String body = Content.Source.asString(request);
        if (body == null || body.isEmpty()) return new JsonObject();
        return new Gson().fromJson(body, JsonObject.class);
    }

    // ── JsonObject → 200 + application/json 으로 응답 ──
    private static void writeJson(Response response, Callback callback, JsonObject json) {
        response.setStatus(200);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json");
        Content.Sink.write(response, true, json.toString(), callback);  // callback까지 처리됨
    }

    // ── GET 쿼리파라미터 한 개 꺼내기 (/path?key=value) — 순수 문자열 파싱(안전) ──
    private static String valueOf(Request request, String key) {
        String q = request.getHttpURI().getQuery();    // "id=100a&page=1" 또는 null
        if (q == null) return null;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv[0].equals(key)) return kv.length > 1 ? kv[1] : "";
        }
        return null;
    }
}
// 사용: main에서 JsonServer.start(); 한 줄
// 포트 / switch 경로·로직만 수정 — readJson/writeJson/valueOf는 그대로 복붙
// 빈 200만 줄 거면 writeJson 대신: response.setStatus(200); callback.succeeded();
```


---

## 7. Jetty 12 HTTP Client (Microservice 호출)

### 7-1. 기본 GET 호출
```java
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ContentResponse;
// Jetty 12에서 ContentResponse import 경로가 다를 수 있음
// 컴파일 에러 시: org.eclipse.jetty.client.api.ContentResponse (Jetty 9 경로)

HttpClient httpClient = new HttpClient();
httpClient.start();       // start() 안 하면 요청 시 IllegalStateException
ContentResponse resp = httpClient.GET("http://127.0.0.1:8011/create?id=100a");
String body = resp.getContentAsString();
httpClient.stop();
```

### 7-2. 상세 요청
```java
ContentResponse resp = httpClient.newRequest(fullUrl)
        .method("GET")
        .send();
String body = resp.getContentAsString();
int status = resp.getStatus();
```

### 7-3. 전체 흐름: 호출 → 파싱 → 변수 업데이트
```java
HttpClient httpClient = new HttpClient();
httpClient.start();
try {
    String fullUrl = url + makeQuery();
    ContentResponse resp = httpClient.newRequest(fullUrl).method("GET").send();
    JsonObject json = new Gson().fromJson(resp.getContentAsString(), JsonObject.class);
    for (String k : json.keySet()) {
        VariableManager.put(k, json.get(k).getAsString());
    }
} finally {
    httpClient.stop();
}
```

### 7-4. L3-HTTP ActionState.java (그대로 복붙)
```java
import com.google.gson.*;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ContentResponse;
import java.net.URLEncoder;
import java.util.*;

public class ActionState {
    private String url;
    private List<String> parameters;

    public ActionState(String url, List<String> parameters) {
        this.url = url;
        this.parameters = parameters;
    }

    private String makeQuery() throws Exception {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            query.append(i == 0 ? "?" : "&");
            String key = parameters.get(i);
            query.append(URLEncoder.encode(key, "UTF-8"))
                 .append("=")
                 .append(URLEncoder.encode(VariableManager.get(key), "UTF-8"));
        }
        return query.toString();
        // parameters가 빈 List(size==0)면 루프 안 돌아서 "" 반환 → URL 그대로
    }

    public void run() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        try {
            ContentResponse resp = httpClient.newRequest(url + makeQuery())
                    .method("GET").send();
            JsonObject json = new Gson().fromJson(resp.getContentAsString(), JsonObject.class);
            for (String k : json.keySet()) {
                VariableManager.put(k, json.get(k).getAsString());
            }
        } finally {
            httpClient.stop();
        }
    }
}
// 이 파일은 시험에서 수정할 부분 없음 — 그대로 복붙
// L4에서는 8-2 ActionState.java 사용 (extends State + return getNext())
```

### 7-5. L3-HTTP StateManager.java (DTO만 시험지 보고 수정)
```java
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

public class StateManager {
    private static Map<String, ActionState> map = new HashMap<>();

    public static ActionState get(String name) { return map.get(name); }

    public static void load() throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("STATE.JSON")));
        StatesDto dto = new Gson().fromJson(json, StatesDto.class);
        for (Map.Entry<String, StatesDto.StateDto> e : dto.state.entrySet()) {
            StatesDto.StateDto s = e.getValue();
            map.put(e.getKey(), new ActionState(s.url, s.parameters));
        }
    }

    // DTO는 inner static class로 여기 안에 넣어도 됨
    static class StatesDto {
        public Map<String, StateDto> state;
        static class StateDto {
            public String type;
            public String url;
            public List<String> parameters;
        }
    }
}
// DTO 필드명 = JSON key 이름 — 시험지 JSON 보고 확인
// 나머지는 그대로 복붙
```

---

## 7B. Jetty 12.1 HTTP Client 심화 (POST / JSON Body / 멀티스레드)

> 7번이 기본 GET이라면, 여기는 POST·JSON 전송·동시 호출까지. **Jetty 12.1 (최신)** 기준.
> import 경로가 Jetty 9/11과 다름 — 아래 표대로만 쓰면 됨.
>
> | 클래스 | Jetty 12.1 import 경로 |
> |--------|------------------------|
> | HttpClient | `org.eclipse.jetty.client.HttpClient` |
> | ContentResponse | `org.eclipse.jetty.client.ContentResponse` |
> | Request | `org.eclipse.jetty.client.Request` |
> | StringRequestContent (body 전송) | `org.eclipse.jetty.client.StringRequestContent` |
> | HttpMethod | `org.eclipse.jetty.http.HttpMethod` |
>
> ※ 컴파일 에러 시 `org.eclipse.jetty.client.api.ContentResponse` (구버전 경로)도 시도.

### 7B-0. HttpUtil.java — 복붙용 유틸 (이것만 있으면 GET/POST 끝)
```java
import com.google.gson.*;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.StringRequestContent;

/**
 * Jetty 12.1 HTTP Client 래퍼.
 * 매 호출마다 start()/stop() 하므로 쓰기 쉽지만, 반복 호출 많으면
 * 7B-4처럼 client 하나를 재사용하는 게 빠름.
 */
public class HttpUtil {

    /** GET 요청 → 응답 body(String) 반환. 예: get("http://127.0.0.1:8011/create?id=100a") */
    public static String get(String url) throws Exception {
        HttpClient client = new HttpClient();
        client.start();                                   // 안 하면 IllegalStateException
        try {
            ContentResponse resp = client.GET(url);       // 동기(블로킹) 호출
            return resp.getContentAsString();             // resp.getStatus() 로 상태코드도 가능
        } finally {
            client.stop();                                // 안 하면 커넥션 풀 누수 → 이후 요청 hang
        }
    }

    /** POST + JSON body 요청 → 응답 body(String) 반환. */
    public static String postJson(String url, String jsonBody) throws Exception {
        HttpClient client = new HttpClient();
        client.start();
        try {
            ContentResponse resp = client.POST(url)
                    .body(new StringRequestContent("application/json", jsonBody))  // 본문+Content-Type 한 번에
                    .send();                              // 동기 전송
            return resp.getContentAsString();
        } finally {
            client.stop();
        }
    }

    /** 응답 JSON을 그대로 VariableManager에 반영 (L3/L4 빈출 패턴). */
    public static void getAndUpdateVars(String url) throws Exception {
        String body = get(url);
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        for (String k : json.keySet()) {                  // {"key":"eH7bDVXX"} → put("key","eH7bDVXX")
            VariableManager.put(k, json.get(k).getAsString());
        }
    }
}
// 사용: String body = HttpUtil.get(url);
// String body = HttpUtil.postJson(url, "{\"id\":\"100a\"}");
// HttpUtil.getAndUpdateVars(url);
```

### 7B-1. 기본 GET (한 번만 호출할 때)
```java
HttpClient client = new HttpClient();
client.start();
try {
    ContentResponse resp = client.GET("http://127.0.0.1:8011/create?id=100a");
    int status   = resp.getStatus();              // 200, 404 ...
    String body  = resp.getContentAsString();     // 응답 본문 (JSON 문자열 등)
} finally {
    client.stop();
}
```

### 7B-2. POST + JSON Body 전송
```java
import org.eclipse.jetty.client.StringRequestContent;

String jsonBody = "{\"targetPort\": 8001}";       // 동적이면 new Gson().toJson(map)

HttpClient client = new HttpClient();
client.start();
try {
    ContentResponse resp = client.POST("http://localhost:8080/proxy")
            .body(new StringRequestContent("application/json", jsonBody))
            .send();
    String body = resp.getContentAsString();
} finally {
    client.stop();
}
// .body(new StringRequestContent("application/json", ...)) 가 Content-Type 헤더까지 설정
// 추가 헤더 필요하면: .headers(h -> h.put("X-Token", "abc")) 체인
```

### 7B-3. 상세 요청 (메서드/타임아웃/쿼리 직접 제어)
```java
import org.eclipse.jetty.http.HttpMethod;
import java.util.concurrent.TimeUnit;

ContentResponse resp = client.newRequest("http://localhost:8080/path")
        .method(HttpMethod.GET)                   // 또는 .method("GET")
        .timeout(5, TimeUnit.SECONDS)             // 응답 5초 안 오면 예외
        .send();
String body = resp.getContentAsString();
```

### 7B-4. 여러 요청 순차 호출 — client 1개 재사용 (효율적)
```java
// client를 매번 new 하지 말고 하나로 재사용 → start/stop 오버헤드 제거
HttpClient client = new HttpClient();
client.start();
try {
    int[] ports = {8001, 8002, 8001, 8002};
    for (int port : ports) {
        try {
            ContentResponse resp = client.GET("http://localhost:8080/" + port);
            System.out.println("[요청 → " + port + "] " + resp.getContentAsString());
        } catch (Exception e) {
            System.out.println("[요청 실패 → " + port + "] " + e.getMessage());
            // try-catch를 루프 안에 → 한 요청 실패해도 다음 요청 계속
        }
    }
} finally {
    client.stop();                                // 모든 요청 끝난 뒤 한 번만 stop
}
```

### 7B-5. 멀티스레드 동시 호출 (ExecutorService)
```java
import java.util.concurrent.*;

// HttpClient는 thread-safe → 하나를 여러 스레드가 공유 가능 (스레드마다 new 금지!)
HttpClient client = new HttpClient();
client.start();
ExecutorService pool = Executors.newFixedThreadPool(5);   // 스레드풀 크기

String[] names = {"Alice", "Bob", "Charlie", "Dana", "Eve"};
for (String name : names) {
    String target = name;                                 // 람다용 effectively final 복사
    pool.submit(() -> {
        try {
            String json = String.format("{\"name\":\"%s\",\"task\":\"TestConnection\"}", target);
            ContentResponse resp = client.POST("http://localhost:8083/proxy")
                    .body(new StringRequestContent("application/json", json))
                    .send();
            System.out.println("[응답] " + target + " → " + resp.getContentAsString());
        } catch (Exception e) {
            System.out.println("[에러] " + target + ": " + e.getMessage());
        }
    });
}

pool.shutdown();                                          // 새 작업 거부, 진행중인 건 마저 실행
pool.awaitTermination(5, TimeUnit.SECONDS);               // 전부 끝날 때까지 최대 5초 대기
client.stop();                                            // 모든 스레드 끝난 뒤 stop
// shutdown()만 하고 awaitTermination() 안 하면 main이 먼저 끝나 결과 못 봄
```

### 7B-6. JsonObject로 바로 주고받기 (파싱 한 줄도 생략)
```java
// HttpUtil(7B-0)에 아래 2개 메서드만 추가하면, 응답을 JsonObject로 바로 받음.
import com.google.gson.*;

/** GET → 응답을 JsonObject로 반환. 예: getJson(url).get("key").getAsString() */
public static JsonObject getJson(String url) throws Exception {
    return new Gson().fromJson(get(url), JsonObject.class);     // get()은 7B-0
}

/** POST(JsonObject body) → 응답을 JsonObject로 반환. */
public static JsonObject postJson(String url, JsonObject body) throws Exception {
    return new Gson().fromJson(postJson(url, body.toString()), JsonObject.class);
    // ↑ postJson(String,String)은 7B-0. 같은 이름 오버로드라 충돌 X
}
```
```java
// ── 사용 예: 6-6 JsonServer와 짝으로 ──
JsonObject body = new JsonObject();
body.addProperty("agentId",   "agent01");
body.addProperty("requestId", "req001");

JsonObject resp = HttpUtil.postJson("http://localhost:8080/monitoring", body);
int correct = resp.get("correct").getAsInt();    // 파싱 코드 없이 바로 사용

// 파일 각 줄을 JSON으로 만들어 연속 POST (실전 패턴)
for (String line : Files.readAllLines(Paths.get("DATA.TXT"))) {
    String[] p = line.split("#");                 // agentId#reqId#type#value
    JsonObject b = new JsonObject();
    b.addProperty("agentId", p[0]);
    b.addProperty("requestId", p[1]);
    HttpUtil.postJson("http://localhost:8080/monitoring", b);
}
```

---

## 8. Workflow 엔진 패턴 (L4-워크플로우 핵심)

### 8-1. Workflow.java (그대로 복붙)
```java
import com.google.gson.*;
import java.util.*;

public class Workflow {
    private String startFrom;
    private List<State> states;
    private List<String> responses;

    public Workflow(String startFrom, List<State> states, List<String> responses) {
        this.startFrom = startFrom;
        this.states = states;
        this.responses = responses;
    }

    private State findState(String name) {
        for (State s : states) {
            if (name.equals(s.getName())) return s;
        }
        return null;
    }

    public JsonObject run() throws Exception {
        String next = startFrom;
        while (!"end".equals(next)) {
            next = findState(next).run();
        }
        return buildResponse();
    }

    private JsonObject buildResponse() {
        JsonObject result = new JsonObject();
        if (responses != null) {
            for (String field : responses) {
                result.addProperty(field, VariableManager.get(field));
            }
        }
        return result;
    }
}
// 이 파일은 시험에서 수정할 부분 없음 — 그대로 복붙
```

### 8-2. ActionState.java (그대로 복붙)
```java
import com.google.gson.*;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ContentResponse;
import java.net.URLEncoder;
import java.util.*;

public class ActionState extends State {
    private String url;
    private List<String> parameters;

    public ActionState(String name, String next, String url, List<String> parameters) {
        super(name, next);
        this.url = url;
        this.parameters = parameters;
    }

    private String makeQuery() throws Exception {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            query.append(i == 0 ? "?" : "&");
            String key = parameters.get(i);
            query.append(URLEncoder.encode(key, "UTF-8"))
                 .append("=")
                 .append(URLEncoder.encode(VariableManager.get(key), "UTF-8"));
        }
        return query.toString();
    }

    @Override
    public String run() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        try {
            ContentResponse resp = httpClient.newRequest(url + makeQuery())
                    .method("GET").send();
            JsonObject json = new Gson().fromJson(resp.getContentAsString(), JsonObject.class);
            for (String k : json.keySet()) {
                VariableManager.put(k, json.get(k).getAsString());
            }
        } finally {
            httpClient.stop();
        }
        return getNext();
    }
}
// 이 파일은 시험에서 수정할 부분 없음 — 그대로 복붙
```

### 8-3. ParallelState.java (그대로 복붙)
```java
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ParallelState extends State {
    private List<Workflow> workflows;

    public ParallelState(String name, String next, List<Workflow> workflows) {
        super(name, next);
        this.workflows = workflows;
    }

    @Override
    public String run() throws Exception {
        CountDownLatch latch = new CountDownLatch(workflows.size());
        for (Workflow w : workflows) {
            new Thread(() -> {
                try {
                    w.run();
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                    latch.countDown();    // catch에서도 반드시 countDown!
                }
            }).start();
        }
        latch.await();
        return getNext();
    }
}
// 이 파일은 시험에서 수정할 부분 없음 — 그대로 복붙
```

### 8-4. ChoiceState.java (그대로 복붙)
```java
import java.util.*;

public class ChoiceState extends State {
    private List<Choice> choices;

    public ChoiceState(String name, String next, List<Choice> choices) {
        super(name, next);
        this.choices = choices;
    }

    @Override
    public String run() throws Exception {
        for (Choice c : choices) {
            if (VariableManager.get(c.variable).equals(c.equal)) {
                return c.next;
            }
        }
        return getNext();
    }

    public static class Choice {
        public String variable, equal, next;
        public Choice(String variable, String equal, String next) {
            this.variable = variable;
            this.equal = equal;
            this.next = next;
        }
    }
}
// 이 파일은 시험에서 수정할 부분 없음 — 그대로 복붙
// choice가 없는 문제면 이 파일 자체를 안 만들어도 됨
```

### 8-5. WorkflowManager.java (DTO만 시험지 보고 수정)
```java
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

public class WorkflowManager {
    private static Map<String, Workflow> map = new HashMap<>();

    public static Workflow get(String name) { return map.get(name); }

    // ── DTO: 시험지 JSON 구조 보고 필드 확인할 것 ──
    static class WorkflowsDto {
        public Map<String, WorkflowDto> workflow; /* JSON 최상위 key 확인 */

        static class WorkflowDto {
            public String startFrom;              /* JSON에 있는지 확인 */
            public Map<String, StateDto> state;
            public List<String> responses;        /* JSON에 있는지 확인 */

            static class StateDto {
                public String type;
                public String next;
                public String url;
                public List<String> parameters;
                public List<WorkflowDto> branches;   // parallel용
                public List<ChoiceDto> choices;       // choice용 (없으면 안 써도 됨)

                static class ChoiceDto {              // choice용 (없으면 안 써도 됨)
                    public String variable;
                    public String equal;
                    public String next;
                }
            }
        }
    }

    // ── load: 그대로 복붙 ──
    public static void load() throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("WORKFLOW.JSON"))); /* 파일명 확인 */
        WorkflowsDto dto = new Gson().fromJson(json, WorkflowsDto.class);
        for (Map.Entry<String, WorkflowsDto.WorkflowDto> e : dto.workflow.entrySet()) {
            map.put(e.getKey(), makeWorkflow(e.getValue()));
        }
    }

    // ── makeWorkflow: 그대로 복붙 ──
    private static Workflow makeWorkflow(WorkflowsDto.WorkflowDto wd) {
        List<State> states = new ArrayList<>();
        for (Map.Entry<String, WorkflowsDto.WorkflowDto.StateDto> e : wd.state.entrySet()) {
            states.add(makeState(e.getKey(), e.getValue()));
        }
        return new Workflow(wd.startFrom, states, wd.responses);
    }

    // ── makeState: type 종류에 따라 case 추가/삭제 ──
    private static State makeState(String name, WorkflowsDto.WorkflowDto.StateDto sd) {
        switch (sd.type) {
            case "action":
                return new ActionState(name, sd.next, sd.url, sd.parameters);
            case "parallel":                          /* parallel 없는 문제면 이 case 삭제 */
                List<Workflow> branches = new ArrayList<>();
                for (WorkflowsDto.WorkflowDto bd : sd.branches) {
                    branches.add(makeWorkflow(bd));
                }
                return new ParallelState(name, sd.next, branches);
            case "choice":                            /* choice 없는 문제면 이 case 삭제 */
                List<ChoiceState.Choice> choices = new ArrayList<>();
                for (WorkflowsDto.WorkflowDto.StateDto.ChoiceDto cd : sd.choices) {
                    choices.add(new ChoiceState.Choice(cd.variable, cd.equal, cd.next));
                }
                return new ChoiceState(name, sd.next, choices);
        }
        return null;
    }
}
// 주석에 "확인"이라고 적힌 곳만 시험지 JSON 보고 확인/수정할 부분
// 나머지는 그대로 복붙
```

### 8-6. MainEntry.java (그대로 복붙)
```java
public class MainEntry {
    public static void main(String[] args) throws Exception {
        VariableManager.load();
        WorkflowManager.load();
        EngineServer.start();
    }
}
// 이 파일은 시험에서 수정할 부분 없음 — 그대로 복붙
```

---

## 9. Thread / 동시성

### 9-1. CountDownLatch (ParallelState 핵심)
```java
import java.util.concurrent.CountDownLatch;   // import 빠뜨리기 쉬움

CountDownLatch latch = new CountDownLatch(n);  // n = branch 수
// 각 스레드에서: latch.countDown();
latch.await();  // 전부 끝날 때까지 블로킹 — n번 countDown 호출되어야 풀림
```

### 9-2. Thread-Safe 자료구조
```java
Map<String, Workflow> map = new ConcurrentHashMap<>();          // 동시 읽기/쓰기
Map<String, String> map = Collections.synchronizedMap(hashMap); // 기존 Map 래핑
```

### 9-3. synchronized 메서드
```java
public synchronized static String get(String key) { return variables.get(key); }
public synchronized static String put(String key, String value) { return variables.put(key, value); }
```

### 9-4. 기본 Thread
```java
Thread t = new Thread(() -> { /* 작업 */ });
t.start();
t.join();  // 종료 대기
```

---

## 10. 외부 프로그램 실행 (ProcessBuilder)

### 10-1. 기본 실행 + 출력 읽기 + 종료 대기
```java
import java.io.*;

ProcessBuilder pb = new ProcessBuilder("MOCK.EXE");  /* 프로그램명 */
pb.directory(new File("."));           // 실행 디렉토리 (상대경로 기준)
pb.redirectErrorStream(true);          // stderr → stdout 합치기
Process process = pb.start();

BufferedReader br = new BufferedReader(
        new InputStreamReader(process.getInputStream()));
String line;
while ((line = br.readLine()) != null) {
    System.out.println(line);
}

int exitCode = process.waitFor();      // 종료될 때까지 대기
```

### 10-2. 인자(argument) 전달
```java
// 방법A: 직접 나열
ProcessBuilder pb = new ProcessBuilder("MOCK.EXE", "8080", "param2");
// → MOCK.EXE 8080 param2 로 실행됨

// 방법B: 변수로 조립
List<String> cmd = new ArrayList<>();
cmd.add("MOCK.EXE");
cmd.add(String.valueOf(port));         /* int → String 변환 필수 */
cmd.add(dataPath);
ProcessBuilder pb = new ProcessBuilder(cmd);
```

### 10-3. 백그라운드 실행 (서버 띄운 뒤 외부 프로그램 실행)
```java
// 서버 start 후 외부 프로그램을 별도 스레드에서 실행
ProcessBuilder pb = new ProcessBuilder("MOCK.EXE");
pb.directory(new File("."));
pb.redirectErrorStream(true);
Process process = pb.start();

// 출력을 별도 스레드에서 읽기 (안 읽으면 버퍼 차서 프로세스 멈춤)
new Thread(() -> {
    try {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}).start();

// waitFor() 안 하면 메인 로직 계속 진행 (비동기)
// 필요시: process.waitFor();
```

### 10-4. 프로세스에 입력 보내기 (stdin)
```java
Process process = pb.start();
OutputStream os = process.getOutputStream();
os.write("input data\n".getBytes());
os.flush();
os.close();   // close 해야 프로세스가 EOF 받음
```

---

## 11. 자료구조 & 유틸리티

### 10-1. HashMap 기본
```java
Map<String, State> map = new HashMap<>();
map.put(key, value);  map.get(key);  map.containsKey(key);

for (Map.Entry<String, State> e : map.entrySet()) {
    String k = e.getKey(); State v = e.getValue();
}
```

### 10-2. 그룹핑 (Key → List)
```java
groups.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
```

### 10-2b. Set — 중복 제거 / 빠른 포함 체크
```java
Set<String> set = new HashSet<>(agentList);   // List → Set: 중복 자동 제거
if (set.contains(id)) { /* ... */ }            // contains는 O(1)

// 정렬된 Map이 필요하면 TreeMap (key 자동 오름차순)
TreeMap<String, List<String>> sorted = new TreeMap<>();
```

### 10-3. 빈도수 카운팅
```java
countMap.merge(item, 1, Integer::sum);                       // 추천
// 또는: countMap.put(item, countMap.getOrDefault(item, 0) + 1);
```

### 10-4. 정렬
```java
Collections.sort(list);                              // 오름차순
Collections.sort(list, Collections.reverseOrder());  // 내림차순

// ── 숫자 포함 문자열 정렬 ("req2" < "req10" 처럼 숫자 크기대로) ──
// 그냥 정렬하면 "req10" < "req2" (사전순) 이 되는 함정!
list.sort((a, b) -> {
    int na = Integer.parseInt(a.replaceAll("[^0-9]", ""));   // 숫자만 추출
    int nb = Integer.parseInt(b.replaceAll("[^0-9]", ""));
    return Integer.compare(na, nb);
});

// ── Map을 value 기준 정렬 (빈도수 Top-N 뽑을 때) ──
List<Map.Entry<String, Integer>> entries = new ArrayList<>(countMap.entrySet());
entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));  // value 내림차순
```

### 10-5. 통계 (합/평균/최대/최소/일치율)
```java
// ── 일치율·정확도 (예측 vs 실제) — "8/10" 형태 빈출 ──
int total = 0, correct = 0;
for (...) { total++; if (predicted.equals(actual)) correct++; }
System.out.println(correct + "/" + total);

// ── 평균 (정수 나눗셈 함정!) ──
long sum = 0; int count = 0;
for (...) { sum += val; count++; }
long avg = count > 0 ? sum / count : 0;   // long/int 정수나눗셈 → 소수점 버림(문제서 정수 보장)
                                          // 0으로 나누기 방지: count>0 체크 필수

// ── 최대/최소 ──
int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
for (int v : values) { max = Math.max(max, v); min = Math.min(min, v); }
```

### 10-6. 시간/날짜 처리 (문자열 그대로 — 빈출!)
```java
// yyyyMMddHHmmss 형식은 "사전순 == 시간순" → 파싱 없이 문자열 비교로 끝
// ── 시간 범위 필터링 ──
String window = "2025041010";              // yyyyMMddHH (시험지가 주는 기준)
String start  = window + "0000";           // 20250410100000
String end    = window + "5959";           // 20250410105959
if (ts.compareTo(start) >= 0 && ts.compareTo(end) <= 0) { /* 범위 내 */ }

// ── 시간대/일별 그룹핑 (substring으로 key 자르기) ──
String hourKey = ts.substring(0, 10);      // yyyyMMddHH (시간대별)
String dateKey = ts.substring(0, 8);       // yyyyMMdd   (일별)
groups.computeIfAbsent(hourKey, k -> new ArrayList<>()).add(item);
// substring(0,10) — Sample.md의 .string()은 오타! 반드시 substring
```

### 10-7. 파일 쓰기
```java
PrintWriter pw = new PrintWriter(new FileWriter("OUTPUT.TXT"));
pw.println("line1");
pw.printf("%s/%d%n", name, count);   // %n = 줄바꿈
pw.close();
```

---

## 12. 실전 체크리스트

```
[ ] 상대경로 사용 — Paths.get("STATE.TXT") O / Paths.get("C:\\TEST\\STATE.TXT") X
[ ] 대소문자 정확히 일치 — "action" vs "Action" 다름, JSON key도 마찬가지
[ ] L1/L2: while(true) 종료 없이 반복 — 없으면 한 번만 실행되고 끝
[ ] L3/L4: server.join() 종료 없이 대기 — 없으면 main 종료와 함께 서버 사라짐
[ ] 디버그 println 전부 삭제 — L1/L2에서 불필요한 출력은 오답 처리
[ ] 파라미터 빈 배열 처리 — "parameters":[] 이면 Query String 안 붙임
[ ] 파라미터 빈 문자열 처리 — "fetch#action#http://.../fetch#" → el[3] 없거나 빈 문자열
[ ] Microservice 응답 → 변수 업데이트 → 다음 State에서 반영 — 가장 빈출 실수
[ ] synchronized / ConcurrentHashMap — ParallelState 때문에 필수
[ ] Jetty 버전 확인 — lib 폴더의 jar 파일명으로 9인지 12인지 확인 후 import 결정
[ ] SP_TEST 먼저 실행 → MOCK.EXE 실행 → "테스트에 성공했습니다!" 확인
[ ] 선행문항 오류 주의 — L3-HTTP 틀리면 L4-워크플로우도 연쇄 실패 가능
[ ] Inner DTO 클래스는 반드시 static — 아니면 Gson이 인스턴스 생성 못 함
[ ] HttpClient stop() 호출 — 안 하면 커넥션 풀 소진으로 이후 요청 행(hang)
[ ] CountDownLatch catch 블록에서도 countDown — 안 하면 예외 시 영원히 대기
[ ] JSON 값 공백 함정 — "P " 처럼 값 끝에 공백 올 수 있음 → .getAsString().trim()
[ ] ID 자릿수 함정 — "req00" vs "req001" vs "req0010" 혼동 주의 (== 비교는 정확히)
[ ] 숫자 정렬 함정 — "req10"이 "req2"보다 앞에 옴(사전순) → 숫자추출 정렬(10-4)
[ ] 정수 나눗셈 — 평균 등 long/int 나눗셈은 소수점 버림, count>0 체크
```

---

## 13. IDE 세팅 / 실행 (IntelliJ·Eclipse 공통)

> 코드가 맞아도 "Jetty/Gson을 못 찾음(빨간 줄, ClassNotFound)"이면 0점. 실행 전 라이브러리부터 잡자.

```
1) 실행: 메인 클래스(MainEntry 등) 열고 → 우클릭 → Run  (또는 ▶ 버튼 / Shift+F10)

2) import가 빨간 줄(org.eclipse.jetty..., com.google.gson...)이면 라이브러리 미연결:
   ─ IntelliJ ─
     File → Project Structure → Libraries → + → Java →
       lib 폴더(또는 jetty/gson jar들) 선택 → OK → Apply
     (Maven 프로젝트면 pom.xml 우클릭 → Maven → Reload Project)
   ─ Eclipse ─
     프로젝트 우클릭 → Build Path → Configure Build Path →
       Libraries 탭 → Add JARs(프로젝트 내 lib) 또는 Add External JARs → Apply

3) Jetty 버전 확인 — 라이브러리에 잡힌 jar 파일명을 보면 됨
     jetty-...-12.x.jar  → 12 import 사용 (이 문서 6번/7B번 그대로)
     jetty-...-9.x.jar   → ContentResponse 등은 org.eclipse.jetty.client.api.* 경로

4) 실행 디렉토리(작업 폴더) 주의 — Paths.get("STATE.TXT")는 "실행 시점 폴더" 기준
     IntelliJ: Run → Edit Configurations → Working directory 를 데이터 파일 있는 폴더로
     안 맞으면 NoSuchFileException → 파일은 맞는데 못 읽는 흔한 함정

5) 서버(L3/L4)는 Run 하면 콘솔이 안 끝나고 떠 있는 게 정상 (server.join() 때문)
     이 상태에서 MOCK.EXE / 테스트 실행 → "테스트에 성공했습니다!" 확인 → 멈춤(■)으로 종료
```

---

## 14. 시험 전략 (코딩 전 5분이 점수를 가른다)

### 14-1. 시작하면 이 순서로 (코딩 전)
```
① 마지막 단계(L4 또는 최종문항)부터 읽어라
   → 최종에 필요한 필드/자료구조를 L1부터 미리 깔아야 나중에 안 뒤엎음
   예) L4에 latency가 필요하면 → L1 VO부터 그 필드 자리를 비워둠

② 데이터 파일 + 기대출력(COMPARE/CMP_*.TXT) 먼저 열기
   → 정답을 보고 거꾸로 코딩. 구분자(#? ,? |?), 필드 수, 함정 확인

③ 자료구조 3줄 설계 (주석으로)
   // 저장소: 무엇을 key로, 무엇을 value로?
   // 흐름:   입력 → 처리 → 출력
   // 확장:   다음 단계에서 더 붙을 것
```

### 14-2. 자료구조 빠른 판단표
```
요청별 1:1 매칭        → Map<id, value> 두 개 만들어 같은 key로 비교
복합키 매칭            → Map<"agentId#requestId", 데이터>
시간대/일별 그룹핑      → Map<ts.substring(0,10), List<항목>>
모델→에이전트 역매핑    → Map<modelName, List<agentId>>  (+ Set으로 중복제거)
빈도수/카운팅          → Map<항목, Integer>  (merge 사용, 10-3)
```

### 14-3. 단계별 코딩 순서 (선행 복사 원칙)
```
L1 완성 → 검증 → 통째로 복사해서 L2 만들기 → 검증 → L3로 복사 → ... → L4
※ 매 단계 "이전 단계 소스 복사 후 확장" — 처음부터 새로 짜지 말 것
※ L3(서버)가 안 되면 L4도 0점 → L3를 확실히 동작시킨 뒤 L4로
```

### 14-4. 막혔을 때 / 시간 부족할 때
```
L1·L2에서 막힘   → 데이터 파일을 손으로 따라가며 기대값을 직접 계산해 비교
L3(서버)에서 막힘 → 서버 뼈대(6번)만이라도 올리고 200 응답부터 통과시키기
시간 부족         → L1·L2 완벽 마무리로 기본 점수 확보 (부분점수 없는 단계는 '동작'에 집중)
```

### 14-5. 제출 직전 30초
```
□ 디버그 println 전부 삭제 (L1/L2 불필요 출력 = 오답)
□ 상대경로인가 (Paths.get("STATE.TXT") O / 절대경로 X)
□ 대소문자 — JSON key, endpoint path, type 문자열 시험지와 정확히 일치
□ 각 단계 폴더에 소스 존재 + 컴파일 OK + 테스트 통과 확인
```
