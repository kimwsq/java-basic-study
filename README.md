# Java Basic Study

Java 파일 입출력, 문자열 처리, 파일/디렉토리 조작에 대한 간단한 예제 코드 모음입니다.

## 디렉토리 구조
- src.io/ : 파일 읽고 쓰기 예제
- src.string/ : 문자열 나누기 및 합치기 예제
- src.file/ : 디렉토리 파일 목록 가져오기 예제



---

## 1. 파일 읽기

### 1-1. TXT 한줄씩 (SUB1/SUB2)
```java
import java.nio.file.Files;
import java.nio.file.Paths;

for (String line : Files.readAllLines(Paths.get("STATE.TXT"))) {
    String[] el = line.split("#");
    // el[0]=name, el[1]=type, el[2]=url, el[3]=파라미터(콤마구분)
    // split("#") 결과 — 끝이 #으로 끝나면 el.length가 3일 수 있음
    // 예: "fetch#action#http://.../fetch#" → el.length == 3 (el[3] 없음!)
    // 반드시 el.length > 3 체크 후 접근할 것
}
```

### 1-2. JSON 통째로 String (SUB3/SUB4)
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

## 2. 콘솔 입출력 (SUB1/SUB2)

### 2-1. Scanner 입력 (솔루션 방식)
```java
Scanner scanner = new Scanner(System.in);
while (true) {                      // 종료 조건 없이 무한 반복 필수!
    String input = scanner.nextLine();
    // 처리 후 출력 — println 한 줄만 출력, 불필요한 로그 출력하면 채점 실패
    System.out.println(result);
}
// while(true)를 빼면 한 번만 실행되고 종료 → MOCK/채점기가 다음 입력 못 보냄 → 0점
```

### 2-2. BufferedReader 입력 (대안)
```java
BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
String input = stdin.readLine().trim();
```

---

## 3. JSON 처리 (Gson)

### 3-1. DTO 클래스로 역직렬화 (SUB3/SUB4 핵심!)
```java
import com.google.gson.Gson;

// JSON 구조에 맞는 DTO 정의 — 필드명이 JSON key와 반드시 일치해야 함!
// "state" 키 → public ... state;  오타나면 null로 파싱되어 NPE 발생
class StatesDto {
    public Map<String, StateDto> state;   // JSON의 "state" 키
    static class StateDto {
        public String type;               // JSON의 "type" 키
        public String url;
        public List<String> parameters;   // JSON의 "parameters" 키 — 빈 배열 []도 정상 파싱됨
    }
}

// 한줄로 파싱 — 내부 클래스에서 사용 시 static class로 선언해야 Gson이 인스턴스 생성 가능
StatesDto dto = new Gson().fromJson(jsonString, StatesDto.class);
```

### 3-2. TypeToken으로 Map 역직렬화 (VariableManager용)
```java
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

// 제네릭 타입(Map<String,String>)은 .class로 전달 불가 → TypeToken 필수
Type type = new TypeToken<Map<String, String>>() {}.getType();  // 중괄호 {} 빠뜨리면 컴파일 에러
Map<String, String> map = new Gson().fromJson(jsonString, type);
```

### 3-3. JsonParser로 직접 파싱
```java
import com.google.gson.*;

JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
String val   = obj.get("name").getAsString();
int num      = obj.get("count").getAsInt();
if (obj.has("key")) { /* 필드 존재 체크 */ }
```

### 3-4. JSON Object 순회
```java
for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
    String key = e.getKey();
    JsonElement val = e.getValue();
}
```

### 3-5. JSON Array 순회
```java
JsonArray arr = obj.getAsJsonArray("items");
for (JsonElement e : arr) {
    String item = e.getAsString();
}
```

### 3-6. JSON 응답 생성
```java
JsonObject result = new JsonObject();
result.addProperty("id", "100a");
result.addProperty("total", "1");
// result.toString() → {"id":"100a","total":"1"}
```

### 3-7. HTTP 응답 파싱 → 변수 업데이트 (SUB3/SUB4 필수 패턴)
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

### 4-1. 단순 VO (SUB1)
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
```

### 4-2. VO에 필드 추가 (SUB2 — keys 추가)
```java
private String[] keys;  // 파라미터 목록, null 가능

public State(String name, String type, String url, String[] keys) { ... }
public String[] getKeys() { return keys; }
```

### 4-3. abstract 클래스 + 상속 (SUB3/SUB4)
```java
public abstract class State {
    private String name;
    private String next;

    public State(String name, String next) {
        this.name = name; this.next = next;
    }
    public String getName() { return name; }
    public String getNext() { return next; }
    public abstract String run() throws Exception;
}

// ActionState extends State  → HTTP 호출
// ParallelState extends State → 병렬 실행
// ChoiceState extends State  → 조건 분기
```

### 4-4. Inner DTO for 복잡한 JSON (WorkflowManager용)
```java
class WorkflowsDto {
    public Map<String, WorkflowDto> workflow;

    static class WorkflowDto {
        public String startFrom;
        public Map<String, StateDto> state;
        public List<String> responses;

        static class StateDto {
            public String type;
            public String next;
            public String url;
            public List<String> parameters;
            public List<WorkflowDto> branches;   // parallel용
            public List<ChoiceDto> choices;       // choice용

            static class ChoiceDto {
                public String variable;
                public String equal;
                public String next;
            }
        }
    }
}
```

### 4-5. Manager 패턴 (static Map + synchronized + load)
```java
public class VariableManager {
    private static Map<String, String> variables;

    // synchronized 빠뜨리면 Parallel에서 동시 접근 시 값이 꼬임
    public synchronized static String get(String key) { return variables.get(key); }
    public synchronized static String put(String key, String value) { return variables.put(key, value); }

    public static void load() throws Exception {
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        String json = new String(Files.readAllBytes(Paths.get("VARIABLE.JSON")));
        variables = Collections.synchronizedMap(new Gson().fromJson(json, type));
        // load()는 main 시작 시 한 번만 호출 — 파일 내용은 업데이트하지 않음 (메모리만 관리)
    }
}
```

---

## 5. Query String 조립

### 5-1. 기본 패턴 (SUB2 콘솔 출력용)
```java
String print = state.getType() + " " + state.getUrl();
String[] keys = state.getKeys();
if (keys != null) {
    for (int i = 0; i < keys.length; i++) {
        print += (i == 0) ? "?" : "&";   // 첫 파라미터 앞은 ?, 나머지는 &
        print += keys[i] + "=" + VariableManager.get(keys[i]);
    }
}
System.out.println(print);
// 파라미터가 없는 State → keys가 null → ?도 &도 안 붙여야 함
// fetch#action#http://.../fetch# → "action http://.../fetch" (뒤에 ? 붙으면 오답)
```

### 5-2. URLEncoder 포함 (SUB3/SUB4 HTTP 호출용)
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

### 6-1. 방식A: Handler.Abstract (Jetty 12 전용, 추천)
```java
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.http.HttpHeader;

Server server = new Server(8080);
server.setHandler(new Handler.Abstract() {
    @Override
    public boolean handle(Request request, Response response,
            Callback callback) throws Exception {

        String path = request.getHttpURI().getPath();  // "/workflow1"
        String method = request.getMethod();           // "GET"
        String name = path.substring(1);               // "workflow1"
        // path가 "/sub/name" 형태면 substring(1)="sub/name" → lastIndexOf("/") 방식이 안전
        // 단, 기출에서는 항상 "/<name>" 형태였으므로 substring(1)로 충분

        // --- 처리 로직 ---
        String result = WorkflowManager.get(name).run().toString();

        // --- 응답 ---
        response.setStatus(200);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json");
        Content.Sink.write(response, true, result, callback);
        return true;
    }
});
server.start();
server.join();
```

### 6-2. 방식A: 빈 200 응답 (SUB3용)
```java
response.setStatus(200);
callback.succeeded();     // callback 호출 안 하면 클라이언트가 응답을 못 받고 타임아웃
return true;              // false 리턴하면 Jetty가 요청을 처리 안 한 것으로 판단
```

### 6-3. 방식A: 요청 Body 읽기 (POST용)
```java
String body = Content.Source.asString(request);
```

### 6-4. 방식B: Servlet (Jetty 12 + jakarta.servlet)
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

### 6-5. 방식C: Jetty 9 백업 (시험 환경이 Jetty 9인 경우)
```java
import org.eclipse.jetty.server.handler.AbstractHandler;
import javax.servlet.http.HttpServletRequest;   // Jetty 9에서는 javax
import javax.servlet.http.HttpServletResponse;

server.setHandler(new AbstractHandler() {
    @Override
    public void handle(String target, org.eclipse.jetty.server.Request baseRequest,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        // target = "/workflow1"
        // 처리 후:
        response.setStatus(200);
        response.setContentType("application/json");
        response.getWriter().write(result);
        baseRequest.setHandled(true);
        // setHandled(true) 빠뜨리면 Jetty가 404 반환 → MOCK.EXE 테스트 실패
    }
});
// 시험장에서 Jetty 버전 확인법: lib 폴더의 jar 파일명에 버전 표기됨
// jetty-server-9.x → Jetty 9 / jetty-server-12.x → Jetty 12
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

---

## 8. Workflow 엔진 패턴 (SUB4 핵심)

### 8-1. State Machine 루프 (Workflow.run)
```java
public JsonObject run() throws Exception {
    String next = startFrom;
    while (!"end".equals(next)) {        // "end" 문자열과 비교 — 대소문자 주의 ("End" X)
        State state = findState(next);   // null이면 NPE → JSON key 오타 의심
        next = state.run();              // 각 State가 다음 State 이름 반환
    }
    return buildResponse();
}

private State findState(String name) {
    for (State s : states) {
        if (name.equals(s.getName())) return s;
    }
    return null;
}

private JsonObject buildResponse() {
    JsonObject result = new JsonObject();
    for (String field : responseFields) {
        result.addProperty(field, VariableManager.get(field));
    }
    return result;
}
```

### 8-2. ActionState.run (HTTP 호출 → 변수 업데이트 → 다음 State)
```java
@Override
public String run() throws Exception {
    HttpClient httpClient = new HttpClient();
    httpClient.start();
    try {
        ContentResponse resp = httpClient.newRequest(url + makeQuery())
                .method("GET").send();
        // 응답 파싱 + 변수 업데이트 — 이걸 빠뜨리면 다음 State에서 이전 값 사용
        JsonObject json = new Gson().fromJson(resp.getContentAsString(), JsonObject.class);
        for (String k : json.keySet()) {
            VariableManager.put(k, json.get(k).getAsString());
        }
    } finally {
        httpClient.stop();   // stop 안 하면 커넥션 풀 소진 → 이후 요청 행(hang)
    }
    return getNext();        // 반드시 다음 State 이름 반환 — void로 만들면 루프 동작 안 함
}
```

### 8-3. ParallelState.run (병렬 실행 → 전부 대기)
```java
@Override
public String run() throws Exception {
    CountDownLatch latch = new CountDownLatch(workflows.size());
    for (Workflow w : workflows) {
        new Thread(() -> {
            try {
                w.run();
                latch.countDown();    // run() 성공해야 카운트 감소
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();    // 예외 시에도 countDown 해줘야 await()이 풀림
            }                         // 안 하면 영원히 대기 → MOCK.EXE 타임아웃
        }).start();
    }
    latch.await();                    // 모든 branch 완료까지 블로킹
    return getNext();
}
```

### 8-4. ChoiceState.run (조건 분기)
```java
@Override
public String run() throws Exception {
    // 순서대로 비교 → 첫 번째 만족하는 조건의 next로 이동 (if-else if-else 구조)
    for (Choice c : choices) {
        // .equals() 순서 주의: variable 값이 null이면 NPE → c.equal.equals(...)로 바꾸면 안전
        if (VariableManager.get(c.variable).equals(c.equal)) {
            return c.next;
        }
    }
    return getNext();  // 모든 조건 불만족 → choice의 "next" (choices 배열 밖의 next)
}

public static class Choice {
    public String variable, equal, next;
    public Choice(String variable, String equal, String next) {
        this.variable = variable; this.equal = equal; this.next = next;
    }
}
```

### 8-5. WorkflowManager.makeState (type별 분기 생성)
```java
private static State makeState(String name, StateDto sd) {
    // type 문자열은 소문자 — "action", "parallel", "choice"
    switch (sd.type) {
        case "action":
            return new ActionState(name, sd.next, sd.url, sd.parameters);
        case "parallel":
            // branches 안의 각 WorkflowDto를 재귀적으로 makeWorkflow 호출
            List<Workflow> branches = new ArrayList<>();
            for (WorkflowDto bd : sd.branches) branches.add(makeWorkflow(bd));
            return new ParallelState(name, sd.next, branches);
        case "choice":
            List<Choice> choices = new ArrayList<>();
            for (ChoiceDto cd : sd.choices) choices.add(new Choice(cd.variable, cd.equal, cd.next));
            return new ChoiceState(name, sd.next, choices);
    }
    return null;  // 여기 도달하면 알 수 없는 type → JSON의 type 오타 확인
}
```

### 8-6. RunManager (SUB3/SUB4 진입점)
```java
public static void main(String[] args) throws Exception {
    // load 순서 중요: Variable 먼저, 그 다음 Workflow/State
    VariableManager.load();
    WorkflowManager.load();   // SUB4: WORKFLOW.JSON
    // StateManager.load();   // SUB3: STATE.JSON

    EngineServer server = new EngineServer();
    server.start();  // 내부에서 server.join() → main이 여기서 블로킹 (종료 안 됨)
    // start() 안에 join()이 없으면 main 종료 → 프로세스 종료 → 서버 사라짐
}
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

## 10. 자료구조 & 유틸리티

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

### 10-3. 빈도수 카운팅
```java
countMap.merge(item, 1, Integer::sum);
```

### 10-4. 정렬
```java
Collections.sort(list);                             // 오름차순
Collections.sort(list, Collections.reverseOrder());  // 내림차순
```

### 10-5. 통계
```java
long sum = 0; int count = 0;
for (...) { sum += val; count++; }
long avg = count > 0 ? sum / count : 0;

int max = Integer.MIN_VALUE;
for (int v : values) max = Math.max(max, v);
```

### 10-6. 문자열 시간 비교
```java
if (ts.compareTo(start) >= 0 && ts.compareTo(end) <= 0) { /* 범위 내 */ }
```

### 10-7. 파일 쓰기
```java
PrintWriter pw = new PrintWriter(new FileWriter("OUTPUT.TXT"));
pw.println("line1");
pw.close();
```

---

## 11. 실전 체크리스트

```
[ ] 상대경로 사용 — Paths.get("STATE.TXT") O / Paths.get("C:\\SUB1\\STATE.TXT") X
[ ] 대소문자 정확히 일치 — "action" vs "Action" 다름, JSON key도 마찬가지
[ ] SUB1/2: while(true) 종료 없이 반복 — 없으면 한 번만 실행되고 끝
[ ] SUB3/4: server.join() 종료 없이 대기 — 없으면 main 종료와 함께 서버 사라짐
[ ] 디버그 println 전부 삭제 — SUB1/2에서 불필요한 출력은 오답 처리
[ ] 파라미터 빈 배열 처리 — "parameters":[] 이면 Query String 안 붙임
[ ] 파라미터 빈 문자열 처리 — "fetch#action#http://.../fetch#" → el[3] 없거나 빈 문자열
[ ] Microservice 응답 → 변수 업데이트 → 다음 State에서 반영 — 가장 빈출 실수
[ ] synchronized / ConcurrentHashMap — ParallelState 때문에 필수
[ ] Jetty 버전 확인 — lib 폴더의 jar 파일명으로 9인지 12인지 확인 후 import 결정
[ ] SP_TEST 먼저 실행 → MOCK.EXE 실행 → "테스트에 성공했습니다!" 확인
[ ] 선행문항 오류 주의 — SUB3 틀리면 SUB4도 연쇄 실패 가능
[ ] Inner DTO 클래스는 반드시 static — 아니면 Gson이 인스턴스 생성 못 함
[ ] HttpClient stop() 호출 — 안 하면 커넥션 풀 소진으로 이후 요청 행(hang)
[ ] CountDownLatch catch 블록에서도 countDown — 안 하면 예외 시 영원히 대기
```
