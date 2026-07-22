# Multi-Agent Travel Planner - 호출 흐름 정리

## 전체 아키텍처 개요

```
[ Browser (home.html) ]
        │  HTTP GET /api/ai/chat?message=... (SSE 연결)
        ▼
[ AiController ]  ← Tomcat 요청 스레드
        │  SseEmitter 생성 → return emitter (소켓 유보, 스레드 반환)
        │  CompletableFuture.runAsync() → ForkJoinPool 스레드로 위임
        ▼
[ TravelOrchestrator.execute() ]  ← ForkJoinPool 스레드 (블로킹 허용)
        │  chatClient.call() → 메인 스레드 블로킹
        ▼
[ LLM (GPT/Claude 등) ]
        │  Tool 선택 결정
        ▼
[ @Tool 메서드 실행 ]
        ├─ callAttractionAgent     → Exam03AttractionAgent  → LLM (Structured Output)
        ├─ callRestaurantAgent     → Exam04RestaurantAgent  → LLM (Structured Output)
        ├─ callAccommodationAgent  → Exam05AccommodationAgent → LLM (Structured Output)
        └─ callMultiAgentForTravelPlan (Multi-Agent 흐름)
                │
                ├─ [LLM] parseUserQuery → Requirements 추출
                │
                ├─ [병렬] collectTravelInfoInParallel
                │       ├─ [Thread-1] AttractionAgent → LLM
                │       ├─ [Thread-2] RestaurantAgent → LLM
                │       └─ [Thread-3] AccommodationAgent → LLM
                │
                ├─ [LLM] PlanAgent → Plan (Structured Output)
                ├─ [로직] BudgetAgent → 예산 검증 (LLM 미사용)
                │
                └─ (예산 초과 시) replanWithAdjustedBudget
                        ├─ [병렬] 3 Agent 재검색
                        ├─ [LLM] PlanAgent 재실행
                        └─ [로직] BudgetAgent 재검증
        │
        ▼ (returnDirect = true → LLM 재호출 없이 JSON 직렬화)
[ SseEmitter ] ── agent 이벤트 (실시간) ──▶ Browser
[ SseEmitter ] ── message 이벤트 (최종결과) ──▶ Browser
[ SseEmitter ] ── complete 이벤트 ──────────▶ Browser
```

---

## 1. 클라이언트 계층 (home.html)

### SSE 연결 수립

```javascript
// home.html - sendMessage()
const eventSource = new EventSource(`/api/ai/chat?message=${encodeURIComponent(userMessage)}`);
```

- 사용자가 채팅창에 메시지 입력 후 전송 버튼 클릭
- `EventSource`로 SSE(Server-Sent Events) 연결 수립
- 서버가 응답을 완성할 때까지 연결 유지

### SSE 이벤트 수신 (3종류)

| 이벤트명 | 용도 | 처리 방식 |
|----------|------|-----------|
| `agent` | Agent 실행 상태 (running/complete/warning) | 화면에 배지(badge) 표시 |
| `message` | 최종 응답 데이터 (JSON 또는 텍스트) | 채팅창에 결과 렌더링 |
| `complete` | 스트림 종료 신호 | EventSource 연결 닫기, 입력창 활성화 |

```javascript
// agent 이벤트: 실시간 진행 상황 표시
eventSource.addEventListener('agent', (e) => {
    const { agent, status, message } = JSON.parse(e.data);
    // running → 배지 표시, complete → 배지 완료 처리
    updateAgentBadges(aiMessageDiv, agentStatuses);
});

// message 이벤트: 최종 결과 렌더링
eventSource.addEventListener('message', (e) => {
    const jsonData = JSON.parse(e.data);
    if (Array.isArray(jsonData)) {
        fullResponse = JSON.stringify(jsonData, null, 2);       // 관광지/맛집/숙소 목록
    } else if (jsonData.plan || jsonData.budgetAnalysis) {
        fullResponse = formatTravelPlan(jsonData);              // 전체 여행 계획
    }
});
```

---

## 2. 컨트롤러 계층 (AiController)

### 2-1. SseEmitter와 Servlet 3.0 AsyncContext

`SseEmitter`는 **WebFlux가 아닌 Spring MVC(Servlet 3.0)** 기반입니다.
`return emitter`가 호출되는 순간 Spring MVC 내부에서 `response.startAsync()`가 실행되어 소켓 연결이 유보됩니다.

```
[Tomcat 요청 스레드]
    │
    ├─ SseEmitter 생성
    ├─ CompletableFuture.runAsync() → ForkJoinPool에 작업 등록
    ├─ return emitter
    │       └─ Spring MVC 내부: response.startAsync() 호출
    │               → AsyncContext 생성 (소켓 연결 유보)
    │               → TCP 연결(소켓)은 열린 채 유지
    │
    └─ [Tomcat 요청 스레드 반환] ← 스레드 풀로 반환되어 다른 요청 처리 가능

[ForkJoinPool 스레드] (별도 스레드)
    │
    ├─ travelOrchestrator.execute(...)  ← LLM 호출 블로킹 허용
    ├─ emitter.send("agent", ...)       → AsyncContext가 보유한 소켓에 직접 write
    ├─ emitter.send("message", ...)     → AsyncContext가 보유한 소켓에 직접 write
    └─ emitter.complete()               → AsyncContext.complete() → 소켓 종료
```

> `emitter.send()`는 **어떤 스레드에서 호출하든** AsyncContext가 보유한 소켓 참조를 통해 데이터를 전송합니다.
> `SseEmitter`는 Bean이 아닌 단순 Java 객체이며, 내부에 AsyncContext의 `response` 참조를 보유합니다.

### 2-2. WebFlux와의 비교

| 구분 | Spring MVC + SseEmitter | Spring WebFlux |
|------|------------------------|----------------|
| 기반 | Servlet 3.0 AsyncContext | Reactor Netty / Non-blocking I/O |
| 스레드 모델 | ForkJoinPool 스레드가 블로킹 작업 수행 | 이벤트 루프 + 논블로킹 |
| emitter.send() | 어떤 스레드에서든 소켓에 직접 write | Flux/Mono 파이프라인으로 전달 |
| LLM 호출 블로킹 | ✅ 허용 (ForkJoinPool 스레드) | ❌ 불가 (별도 스케줄러 필요) |
| Bean 등록 여부 | ❌ 단순 Java 객체 | ❌ 마찬가지 |

### 2-3. 코드 흐름

```java
@GetMapping("/ai/chat")
public SseEmitter chat(@RequestParam("message") String userQuery, HttpSession session) {
    String sessionId = session.getId();
    SseEmitter emitter = new SseEmitter(300000L); // 5분 타임아웃

    // ForkJoinPool 스레드에 작업 위임 (LLM 호출 블로킹을 Tomcat 스레드 밖으로 이동)
    CompletableFuture.runAsync(() -> {
        try {
            String response = travelOrchestrator.execute(userQuery, sessionId, emitter);
            sendSseEvent(emitter, "message", response);  // 최종 결과 전송
            sendSseEvent(emitter, "complete", "");        // 완료 신호
            emitter.complete();                           // 소켓 종료
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });

    return emitter; // Tomcat 스레드 반환, AsyncContext로 소켓 유보
}
```

**핵심 포인트:**
- `return emitter` 시점에 Tomcat 스레드는 해제되지만 TCP 소켓 연결은 유지됨
- LLM 호출(수십 초 블로킹)을 ForkJoinPool 스레드에서 처리 → Tomcat 스레드 고갈 방지
- `emitter`를 Orchestrator에 파라미터로 전달 → Agent 실행 상태를 실시간으로 클라이언트에 전송

---

## 3. Orchestrator 계층 (TravelOrchestrator)

### 3-1. LLM이 Tool을 선택하는 구조

```java
public String execute(String userQuery, String conversationId, SseEmitter emitter) {
    ChatClient chatClient = chatClientBuilder.build();

    String response = chatClient.prompt()
        .system(systemMessage)          // Tool 사용 규칙 주입
        .user(userQuery)                // 사용자 질문
        .advisors(MessageChatMemoryAdvisor...)   // 대화 기억 (ChatMemory)
        .tools(this)                    // @Tool 메서드 4개 등록
        .toolContext(Map.of("emitter", emitter)) // SSE 통로를 Tool에 전달
        .call()
        .content();                     // 메인 스레드 블로킹 대기

    return response;
}
```

**LLM이 판단하는 Tool 라우팅:**

| 사용자 입력 예시 | LLM이 선택하는 Tool |
|-----------------|-------------------|
| "제주도 명소" | `callAttractionAgent` |
| "강남 맛집" | `callRestaurantAgent` |
| "제주도 펜션" | `callAccommodationAgent` |
| "제주도 2박3일 계획 짜줘" | `callMultiAgentForTravelPlan` |
| "안녕" (일반 대화) | Tool 미호출, LLM 직접 답변 |

### 3-2. ToolContext로 emitter 전달

```java
// emitter를 ToolContext에 넣어 @Tool 메서드로 전달
.toolContext(Map.of("emitter", emitter))

// @Tool 메서드에서 꺼내기
private SseEmitter getEmitter(ToolContext toolContext) {
    return (SseEmitter) toolContext.getContext().get("emitter");
}
```

> ThreadLocal 없이 emitter를 @Tool 메서드에 안전하게 전달하는 패턴

### 3-3. returnDirect = true의 의미

```java
@Tool(description = "...", returnDirect = true)
public List<Attraction> callAttractionAgent(String query, ToolContext toolContext) { ... }
```

| | returnDirect = false (기본값) | returnDirect = true |
|--|-------------------------------|---------------------|
| Tool 실행 후 | Tool 결과를 LLM에 재전달 | LLM 재호출 없음 |
| LLM이 하는 일 | Tool 결과를 자연어로 변환 | 없음 |
| 최종 반환 | 자연어 텍스트 | Tool 반환값을 JSON 직렬화 |
| 적합한 경우 | 자연어 설명이 필요할 때 | 구조화된 JSON이 필요할 때 |

**이 프로젝트에서 returnDirect = true를 사용하는 이유:**
1. `List<Attraction>`, `List<Restaurant>`, `Plan` 등 이미 완성된 구조화 데이터를 그대로 프론트엔드에 전달
2. 내부 Agent들이 이미 LLM을 통해 처리 완료 → LLM 재호출은 낭비
3. LLM이 자연어로 변환하면 JSON 구조가 깨져 프론트엔드 파싱 불가

---

## 4. Multi-Agent 흐름 (callMultiAgentForTravelPlan)

"N박N일 계획" 요청 시의 상세 실행 흐름:

```
callMultiAgentForTravelPlan("제주도 2박3일 50만원 예산으로 여행 계획 짜줘")
│
│ [단계 1] 요구사항 파싱
├─ parseUserQuery()
│       └─ LLM 호출 → entity(Requirements.class) [Structured Output]
│               → { destination: "제주도", days: 3, maxBudget: 500000 }
│               → PlanState에 저장
│
│ [단계 2] 병렬 정보 수집
├─ collectTravelInfoInParallel()
│       ├─ CompletableFuture.runAsync() → [Thread-1] AttractionAgent.execute(state)
│       │       └─ LLM 호출 → List<Attraction> [Structured Output]
│       ├─ CompletableFuture.runAsync() → [Thread-2] RestaurantAgent.execute(state)
│       │       └─ LLM 호출 → List<Restaurant> [Structured Output]
│       └─ CompletableFuture.runAsync() → [Thread-3] AccommodationAgent.execute(state)
│               └─ LLM 호출 → List<Accommodation> [Structured Output]
│       └─ CompletableFuture.allOf(...).join() → 3개 완료까지 대기
│
│ [단계 3] 여행 일정 생성
├─ PlanAgent.execute(state)
│       └─ LLM 호출 (관광지+맛집+숙소 목록을 컨텍스트로 전달)
│               → Plan 객체 [Structured Output]
│
│ [단계 4] 예산 검증
├─ BudgetAgent.execute(state)   ← LLM 미사용, 순수 Java 계산 로직
│       → 총 비용 계산 및 예산 초과 여부 판단
│
│ [단계 5] 피드백 루프 (조건부)
└─ if (budgetAnalysis.isExceeded()) → replanWithAdjustedBudget()
        ├─ [병렬] 3 Agent 재검색 (저렴한 옵션)
        ├─ PlanAgent.execute(state) 재실행  → LLM 호출
        └─ BudgetAgent.execute(state) 재검증
```

### PlanState - 에이전트 간 공유 상태 객체

```
PlanState
├─ [입력] userQuery, destination, days, maxBudget  ← parseUserQuery가 채움
├─ [수집] attractions, restaurants, accommodations  ← 각 Agent가 병렬로 채움
├─ [산출] plan, budgetAnalysis                     ← PlanAgent/BudgetAgent가 채움
└─ [제어] replan, previousTotalCost                ← 피드백 루프 제어 플래그
```

---

## 5. 전문가 Agent 계층

### 단일 Agent 구조 (Exam03AttractionAgent 예시)

```java
@Component
public class Exam03AttractionAgent {
    private final ChatClient chatClient;           // 전용 ChatClient (System Prompt 고정)
    private final InternetSearchService searchService; // 인터넷 검색 도구

    // PlanState를 통해 호출 (Multi-Agent 흐름)
    public void execute(PlanState state) { ... }

    // query String으로 호출 (단독 Tool 호출 흐름)
    public List<Attraction> execute(String query) { ... }

    @Tool  // LLM이 인터넷 검색 도구를 직접 호출
    public String searchAttractions(String query) { ... }

    @Tool
    public String fetchAttractionInfo(String name) { ... }
}
```

**각 Agent의 LLM 호출 방식:**
- `chatClient.prompt().call().entity(List<Attraction>.class)` → Structured Output
- Agent 내부에서도 `@Tool`을 등록하여 LLM이 검색 도구를 자율적으로 활용

### BudgetAgent - LLM 미사용 Agent

```java
@Component
public class BudgetAgent {
    public void execute(PlanState state) {
        // 순수 Java 계산 로직만 사용
        int totalCost = 관광지비용 + 식사비용 + 숙소비용;
        boolean exceeded = totalCost > state.getMaxBudget();
        state.setBudgetAnalysis(new BudgetAnalysis(...));
    }
}
```

> 모든 Agent가 LLM을 호출하는 것이 아님. BudgetAgent는 계산 로직만 사용.

---

## 6. LLM 호출 횟수 요약

"제주도 2박3일 50만원 여행 계획" 요청 시 (예산 초과 없는 경우):

| 순서 | 호출 주체 | 역할 | LLM 호출 |
|------|-----------|------|----------|
| 1 | TravelOrchestrator | Tool 선택 (callMultiAgentForTravelPlan 결정) | ✅ 1회 |
| 2 | parseUserQuery | 요구사항 추출 (Requirements Structured Output) | ✅ 1회 |
| 3 | AttractionAgent | 관광지 추천 (병렬) | ✅ 1~2회 |
| 4 | RestaurantAgent | 맛집 추천 (병렬) | ✅ 1~2회 |
| 5 | AccommodationAgent | 숙소 추천 (병렬) | ✅ 1~2회 |
| 6 | PlanAgent | 여행 일정 생성 | ✅ 1회 |
| 7 | BudgetAgent | 예산 계산 | ❌ 없음 |
| - | returnDirect = true | 최종 결과 반환 | ❌ 없음 |

> 최소 6회 이상의 LLM 호출이 발생. 예산 초과 시 재계획으로 추가 호출.

---

## 7. SSE 이벤트 타임라인

```
시간 →

[Client]  → GET /api/ai/chat?message=...  (SSE 연결)
[Server]  ← emitter 반환 (연결 유지)

           agent: {AttractionAgent, running}   ──▶ 브라우저 배지 표시
           agent: {RestaurantAgent, running}   ──▶ 브라우저 배지 표시
           agent: {AccommodationAgent, running} ──▶ 브라우저 배지 표시
                 (병렬 처리 중...)
           agent: {AttractionAgent, complete}  ──▶ 브라우저 배지 완료
           agent: {RestaurantAgent, complete}  ──▶ 브라우저 배지 완료
           agent: {AccommodationAgent, complete} ──▶ 브라우저 배지 완료

           agent: {PlanAgent, running}         ──▶ 브라우저 배지 표시
           agent: {PlanAgent, complete}        ──▶ 브라우저 배지 완료

           agent: {BudgetAgent, running}       ──▶ 브라우저 배지 표시
           agent: {BudgetAgent, complete}      ──▶ 총비용/예산 메시지 표시

           message: {Plan JSON}               ──▶ 채팅창에 여행 계획 렌더링
           complete: ""                       ──▶ SSE 연결 종료
```

---

## 8. 핵심 설계 패턴 정리

| 패턴 | 적용 위치 | 목적 |
|------|-----------|------|
| **Tool 기반 라우팅** | TravelOrchestrator | LLM이 질문 유형에 따라 자동으로 적절한 Agent 선택 |
| **ToolContext** | emitter 전달 | ThreadLocal 없이 @Tool 메서드에 런타임 객체 전달 |
| **returnDirect = true** | 모든 @Tool 메서드 | 구조화된 JSON을 LLM 재처리 없이 그대로 반환 |
| **Structured Output** | 각 Agent | LLM 응답을 Java 객체로 직접 역직렬화 |
| **PlanState (공유 상태)** | Multi-Agent 흐름 | 에이전트 간 데이터 전달 및 흐름 제어 |
| **CompletableFuture 병렬** | collectTravelInfoInParallel | 독립적인 Agent를 동시에 실행하여 응답 시간 단축 |
| **SSE 스트리밍** | AiController → home.html | 긴 처리 시간 동안 실시간 진행 상황을 사용자에게 표시 |
| **Servlet AsyncContext** | AiController | return emitter로 Tomcat 스레드 반환, 소켓은 유보하여 ForkJoinPool에서 비동기 write |
| **피드백 루프** | BudgetAgent → replan | 결과 검증 후 조건부 재실행으로 품질 보장 |
