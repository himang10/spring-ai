# Spring AI Tool Calling 메커니즘 가이드

## 개요

이 문서는 Spring AI의 Tool Calling 메커니즘이 어떻게 작동하는지, LLM과 Spring AI, Tool 간의 호출 흐름과 데이터 구조를 상세히 설명합니다.

**핵심 개념:**
- Spring AI는 `@Tool` 어노테이션을 Function Calling 프로토콜로 변환
- `chatClient.call()` 내부에서 Tool Call 루프가 자동으로 반복 처리
- HTTP REST API 기반 통신 (세션 연결 없음)
- Tool 실행 시 `toolContext`를 자동 주입

---

## 전체 아키텍처

```
[Application] 
    ↓
[ChatClient] 
    ↓
[Spring AI Framework]
    ↓
[ChatModel] → [LLM API (HTTP)]
    ↑              ↓
    └──────────────┘
         Tool Call Loop
    ↓
[Tool Methods (@Tool)]
    ↓
[Result] → [LLM] → [Final Response]
```

---

## 1. Tool 등록 및 변환 과정

### 1.1 Java 코드에서 Tool 정의

```java
@Component
public class HeatingSystemTools {
  
  @Tool(description = "현재 온도를 제공합니다.")
  public int getTemperature() {
    Random random = new Random();
    int temperature = random.nextInt(13) + 18;
    return temperature;
  }
  
  @Tool(description = "타겟 온도까지 난방 시스템을 가동합니다.")
  public String startHeatingSystem(
    @ToolParam(description = "타겟 온도", required = true) int targetTemperature,
    ToolContext toolContext) {
    // Tool 실행 로직
    return "success";
  }
}
```

### 1.2 Spring AI가 Tool 정보를 수집

`chatClient.prompt().tools(heatingSystemTools)` 호출 시:

1. `@Component`로 등록된 `HeatingSystemTools` 객체를 스캔
2. `@Tool` 어노테이션이 붙은 메서드들을 추출
3. 메서드 시그니처, 파라미터, 반환 타입을 분석
4. Function Calling 스키마로 변환

### 1.3 LLM에 전달되는 Function 스키마

```json
{
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "getTemperature",
        "description": "현재 온도를 제공합니다.",
        "parameters": {
          "type": "object",
          "properties": {},
          "required": []
        }
      }
    },
    {
      "type": "function",
      "function": {
        "name": "startHeatingSystem",
        "description": "타겟 온도까지 난방 시스템을 가동합니다.",
        "parameters": {
          "type": "object",
          "properties": {
            "targetTemperature": {
              "type": "integer",
              "description": "타겟 온도"
            }
          },
          "required": ["targetTemperature"]
        }
      }
    }
  ]
}
```

**변환 규칙:**
- `@Tool(description = "...")` → `function.description`
- 메서드 이름 → `function.name`
- `@ToolParam(description = "...", required = true)` → `parameters.properties`와 `required` 배열
- Java 타입 → JSON Schema 타입 (int → integer, String → string, List → array 등)

---

## 2. ChatClient 호출 및 초기 요청

### 2.1 코드 예시

```java
String answer = chatClient.prompt()
    .system("현재 온도가 사용자가 원하는 온도 이상이라면 난방 시스템을 중지하세요.")
    .user(question)
    .tools(heatingSystemTools)
    .toolContext(Map.of("controlKey", ""))
    .call()
    .content();
```

### 2.2 Spring AI → LLM 초기 요청 구조

```json
{
  "messages": [
    {
      "role": "system",
      "content": "현재 온도가 사용자가 원하는 온도 이상이라면 난방 시스템을 중지하세요."
    },
    {
      "role": "user",
      "content": "온도를 25도로 설정해줘"
    }
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "getTemperature",
        "description": "현재 온도를 제공합니다.",
        "parameters": {
          "type": "object",
          "properties": {},
          "required": []
        }
      }
    },
    {
      "type": "function",
      "function": {
        "name": "startHeatingSystem",
        "description": "타겟 온도까지 난방 시스템을 가동합니다.",
        "parameters": {
          "type": "object",
          "properties": {
            "targetTemperature": {
              "type": "integer",
              "description": "타겟 온도"
            }
          },
          "required": ["targetTemperature"]
        }
      }
    }
  ]
}
```

**중요 사항:**
- `toolContext`는 LLM에 전달되지 않음
- `toolContext`는 Tool 메서드 실행 시 Spring AI가 자동으로 주입
- HTTP REST API로 전송 (세션 연결 없음)

---

## 3. LLM의 Tool Call 결정 및 응답

### 3.1 LLM의 사고 과정

LLM은 사용자 질문과 사용 가능한 Tool을 분석하여:
1. 필요한 정보를 파악
2. 적절한 Tool 선택
3. Tool 호출에 필요한 파라미터 결정

**예시:** "온도를 25도로 설정해줘"
- 현재 온도 확인 필요 → `getTemperature()` 호출
- 설정 온도와 비교 → `startHeatingSystem()` 또는 `stopHeatingSystem()` 호출

### 3.2 LLM → Spring AI Tool Call 응답

```json
{
  "role": "assistant",
  "content": null,
  "tool_calls": [
    {
      "id": "call_temp_001",
      "type": "function",
      "function": {
        "name": "getTemperature",
        "arguments": "{}"
      }
    }
  ]
}
```

**필드 설명:**
- `role: "assistant"`: LLM(어시스턴트)이 보낸 메시지
- `content: null`: Tool Call만 있고 텍스트 응답은 없음
- `tool_calls`: 호출할 Tool 목록
  - `id`: Tool Call 고유 식별자
  - `type: "function"`: Function Calling 타입
  - `function.name`: 호출할 Tool 메서드 이름
  - `function.arguments`: JSON 문자열로 인코딩된 파라미터

---

## 4. Spring AI의 Tool 실행

### 4.1 Tool Call 감지 및 메서드 호출

Spring AI는 `tool_calls` 배열을 감지하고:

1. `function.name`으로 등록된 Tool 메서드 찾기
2. `function.arguments` JSON을 파싱하여 Java 객체로 변환
3. `toolContext` 주입 (`.toolContext()`로 설정한 값)
4. Java 메서드 실행

**의사 코드:**
```java
// Spring AI 내부 처리
for (ToolCall toolCall : response.getToolCalls()) {
    String functionName = toolCall.getFunction().getName();
    String argumentsJson = toolCall.getFunction().getArguments();
    
    // Tool 메서드 찾기
    Method toolMethod = findToolMethod(functionName);
    
    // JSON 파라미터를 Java 객체로 변환
    Object[] args = parseArguments(argumentsJson, toolMethod);
    
    // toolContext 주입
    if (toolMethod.hasToolContextParameter()) {
        ToolContext toolContext = new ToolContext();
        toolContext.getContext().putAll(toolContextMap); // .toolContext()로 설정한 값
        args = injectToolContext(args, toolContext);
    }
    
    // Tool 메서드 실행
    Object result = toolMethod.invoke(toolInstance, args);
    
    // 결과를 JSON으로 직렬화
    String resultJson = serializeToJson(result);
    
    // 메시지 히스토리에 추가
    addToolResultToHistory(toolCall.getId(), functionName, resultJson);
}
```

### 4.2 실제 Tool 메서드 실행

```java
// getTemperature() 호출
int currentTemp = heatingSystemTools.getTemperature();
// 예: 22 반환

// startHeatingSystem() 호출 (toolContext 주입)
ToolContext toolContext = new ToolContext();
toolContext.getContext().put("controlKey", ""); // .toolContext()로 설정한 값

String result = heatingSystemTools.startHeatingSystem(25, toolContext);
// 예: "success" 또는 "failure" 반환
```

---

## 5. Tool 결과를 LLM에 전달

### 5.1 Tool 실행 결과 메시지

Spring AI는 Tool 실행 결과를 메시지 히스토리에 추가:

```json
{
  "role": "tool",
  "name": "getTemperature",
  "tool_call_id": "call_temp_001",
  "content": "22"
}
```

**필드 설명:**
- `role: "tool"`: Tool 실행 결과임을 나타냄
- `name`: 실행된 Tool 메서드 이름
- `tool_call_id`: 원본 Tool Call의 `id`와 매칭
- `content`: Tool 실행 결과 (JSON 문자열로 직렬화)

### 5.2 복잡한 객체 반환 시

```java
@Tool(description = "디렉토리 항목 조회")
public List<Item> listFiles(String relativePath) {
    // ...
    return list; // List<Item> 반환
}
```

**LLM에 전달되는 형식:**
```json
{
  "role": "tool",
  "name": "listFiles",
  "tool_call_id": "call_listfiles_001",
  "content": "[{\"path\":\"/Users/.../file1.txt\",\"isDirectory\":false},{\"path\":\"/Users/.../folder1\",\"isDirectory\":true}]"
}
```

### 5.3 void 반환 타입

```java
@Tool(description = "지정된 시간에 알람을 설정합니다.")
public void setAlarm(String time) {
    // 알람 설정 로직
    log.info("알람 설정 시간: " + time);
}
```

**LLM에 전달되는 형식:**
```json
{
  "role": "tool",
  "name": "setAlarm",
  "tool_call_id": "call_alarm_001",
  "content": "null"
}
```

---

## 6. Tool Call 루프

### 6.1 chatClient.call() 내부 처리 흐름

```
chatClient.call() 실행
    ↓
1. Prompt 구성
   - 메시지 (system, user)
   - Tool 정보 (Function 스키마)
    ↓
2. LLM API 호출 (HTTP POST)
   - ChatModel을 통해 LLM 제공자 API 호출
    ↓
3. LLM 응답 수신
   - Tool Call 포함 여부 확인
    ↓
4. Tool Call이 있는가?
   ├─ YES → 5단계로
   └─ NO → 9단계로 (최종 응답)
    ↓
5. Tool 메서드 실행
   - toolContext 주입
   - Java 메서드 호출
   - 결과 수집
    ↓
6. Tool 결과를 메시지 히스토리에 추가
   - role: "tool" 메시지 추가
    ↓
7. LLM API 재호출
   - 업데이트된 메시지 히스토리 전송
    ↓
8. 3단계로 돌아가서 반복
    ↓
9. 최종 응답 반환
   - content 필드의 텍스트 반환
```

### 6.2 전체 메시지 히스토리 예시

```json
{
  "messages": [
    {
      "role": "system",
      "content": "현재 온도가 사용자가 원하는 온도 이상이라면 난방 시스템을 중지하세요."
    },
    {
      "role": "user",
      "content": "온도를 25도로 설정해줘"
    },
    {
      "role": "assistant",
      "content": null,
      "tool_calls": [
        {
          "id": "call_temp_001",
          "type": "function",
          "function": {
            "name": "getTemperature",
            "arguments": "{}"
          }
        }
      ]
    },
    {
      "role": "tool",
      "name": "getTemperature",
      "tool_call_id": "call_temp_001",
      "content": "22"
    },
    {
      "role": "assistant",
      "content": null,
      "tool_calls": [
        {
          "id": "call_heating_001",
          "type": "function",
          "function": {
            "name": "startHeatingSystem",
            "arguments": "{\"targetTemperature\": 25}"
          }
        }
      ]
    },
    {
      "role": "tool",
      "name": "startHeatingSystem",
      "tool_call_id": "call_heating_001",
      "content": "success"
    },
    {
      "role": "assistant",
      "content": "현재 온도는 22도입니다. 25도까지 난방 시스템을 가동했습니다.",
      "tool_calls": null
    }
  ]
}
```

---

## 7. toolContext 동작 방식

### 7.1 toolContext 설정

```java
chatClient.prompt()
    .tools(heatingSystemTools)
    .toolContext(Map.of("controlKey", "heatingSystemKey"))
    .call()
```

### 7.2 Tool 메서드에서 toolContext 사용

```java
@Tool(description = "타겟 온도까지 난방 시스템을 가동합니다.")
public String startHeatingSystem(
    @ToolParam(description = "타겟 온도", required = true) int targetTemperature,
    ToolContext toolContext) {
    
    // toolContext에서 값 추출
    String controlKey = (String) toolContext.getContext().get("controlKey");
    
    if (controlKey != null && controlKey.equals("heatingSystemKey")) {
        // 권한 확인 성공
        return "success";
    } else {
        // 권한 없음
        return "failure";
    }
}
```

### 7.3 toolContext 특징

- **LLM에 전달되지 않음**: Tool 스키마에 포함되지 않음
- **Spring AI가 자동 주입**: Tool 메서드 실행 시 `ToolContext` 파라미터가 있으면 자동 주입
- **보안/권한 관리**: Tool 실행 시 추가 컨텍스트 정보 제공
- **세션별 관리**: 각 `chatClient.call()` 호출마다 독립적인 toolContext

---

## 8. 통신 프로토콜 및 세션

### 8.1 HTTP REST API 기반

- **세션 연결 없음**: 각 API 호출은 독립적인 HTTP 요청/응답
- **LLM 제공자 API**: OpenAI, Anthropic, Google 등 각 제공자의 REST API 사용
- **ChatModel 구현체**: 각 LLM 제공자별 구현체가 HTTP 클라이언트 역할

### 8.2 chatClient 내부 처리

`chatClient.call()` 메서드는:
- **동기 처리**: Tool Call 루프가 완료될 때까지 블로킹
- **자동 반복**: 최종 응답이 나올 때까지 자동으로 Tool Call 루프 반복
- **메시지 히스토리 관리**: 모든 메시지를 자동으로 누적하여 전송
- **에러 처리**: Tool 실행 실패 시 LLM에 에러 메시지 전달

### 8.3 세션 vs 단일 호출

**세션이 아닌 이유:**
- 각 `chatClient.call()` 호출은 독립적
- HTTP 요청/응답 기반 (연결 유지 없음)
- 단일 호출 내에서 Tool Call 루프 완료

**단일 호출 내 처리:**
```
Application → chatClient.call()
                ↓
            [Tool Call Loop]
                ↓
         최종 응답 반환
```

---

## 9. 데이터 구조 요약

### 9.1 Message Role 종류

| Role | 설명 | content 필드 |
|------|------|--------------|
| `user` | 사용자 메시지 | 질문 텍스트 |
| `assistant` | LLM 응답 | 텍스트 응답 또는 `null` (Tool Call 시) |
| `tool` | Tool 실행 결과 | JSON 문자열 (Tool 결과) |
| `system` | 시스템 프롬프트 | 시스템 지시사항 |

### 9.2 Tool Call 구조

```json
{
  "id": "call_xxx",
  "type": "function",
  "function": {
    "name": "methodName",
    "arguments": "{\"param1\": \"value1\", \"param2\": 123}"
  }
}
```

### 9.3 Tool Result 구조

```json
{
  "role": "tool",
  "name": "methodName",
  "tool_call_id": "call_xxx",
  "content": "result_json_string"
}
```

---

## 10. 핵심 정리

### 10.1 전체 흐름 요약

1. **Tool 등록**: `@Tool` 어노테이션 → Function 스키마 변환
2. **초기 요청**: 질문 + Tool 정보 → LLM
3. **Tool Call**: LLM → Tool 호출 결정 → Spring AI
4. **Tool 실행**: Spring AI → Java 메서드 호출 (toolContext 주입)
5. **결과 전달**: Tool 결과 → LLM
6. **반복**: 최종 응답까지 3-5 단계 반복
7. **최종 응답**: LLM → 텍스트 응답 → Application

### 10.2 주요 특징

- ✅ **자동화**: 개발자는 `@Tool`만 작성하면 자동 처리
- ✅ **타입 안전성**: Java 타입 ↔ JSON 자동 변환
- ✅ **컨텍스트 관리**: toolContext로 추가 정보 제공
- ✅ **멀티 턴**: 여러 Tool을 순차적으로 호출 가능
- ✅ **에러 처리**: Tool 실행 실패 시 LLM에 자동 전달

### 10.3 주의사항

- ⚠️ `toolContext`는 LLM에 전달되지 않음
- ⚠️ Tool 메서드의 반환 타입은 JSON으로 직렬화 가능해야 함
- ⚠️ `void` 반환 시 `content`는 `null`로 전달됨
- ⚠️ Tool Call 루프는 `chatClient.call()` 내부에서 동기적으로 처리됨

---

## 참고 자료

- Spring AI 공식 문서
- OpenAI Function Calling API
- Tool-chain-flow.md (프로젝트 내 상세 예시)

