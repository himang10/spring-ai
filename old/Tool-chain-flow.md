---
marp: true
theme: default
size: 16:9
paginate: true
backgroundColor: '#f8f9fa'
color: '#212529'

style: |
  section {
    font-family: 'Malgun Gothic', '맑은 고딕', Arial, sans-serif;
    padding: 50px;
    line-height: 1.6;
  }

  /* 제목 스타일 */
  h1 {
    color: #f9a825;
    border-bottom: 4px solid #f9a825;
    padding-bottom: 10px;
    margin-bottom: 30px;
  }

  h2 {
    color: #fbc02d;
    margin-top: 40px;
    border-left: 5px solid #fbc02d;
    padding-left: 10px;
  }

  h3 {
    color: #ffca28;
    margin-top: 25px;
  }

  /* 인라인 코드 */
  :not(pre) > code {
    background-color: #f1f3f5;
    color: #d63384;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.75em;
    font-family: 'Fira Code', 'Consolas', monospace;
  }

  /* 블록 코드 */
  pre {
    background-color: #ffffff;   /* ✅ 흰색 배경 */
    color: #212529;              /* 기본 글자색 (검정 계열) */
    padding: 14px;
    border-radius: 6px;
    font-size: 0.7em;            /* ✅ 폰트 두 단계 축소 */
    overflow-x: auto;
    font-family: 'Fira Code', 'Consolas', monospace;
    border: 1px solid #dee2e6;   /* 경계선 살짝 추가 */
  }

  /* 하이라이트 색상 (optional, 색 보정) */
  .hljs-keyword { color: #0074d9; font-weight: bold; }
  .hljs-string { color: #d63384; }
  .hljs-number { color: #e67e22; }
  .hljs-comment { color: #6c757d; font-style: italic; }
  .hljs-title, .hljs-class, .hljs-function { color: #2e86de; }
  .hljs-attr, .hljs-variable { color: #17a2b8; }

  /* 인용구 (Tip/Note) */
  blockquote {
    border-left: 5px solid #f9a825;
    padding-left: 15px;
    margin-left: 0;
    color: #444;
    background: #fffde7;
    border-radius: 5px;
    padding-top: 10px;
    padding-bottom: 10px;
  }

  /* 박스 스타일 */
  .tip {
    background-color: #e3f2fd;
    border-left: 5px solid #2196f3;
    padding: 10px 15px;
    border-radius: 5px;
    margin: 15px 0;
  }

  .warning {
    background-color: #fff3e0;
    border-left: 5px solid #ff9800;
    padding: 10px 15px;
    border-radius: 5px;
    margin: 15px 0;
  }

  .danger {
    background-color: #ffebee;
    border-left: 5px solid #e53935;
    padding: 10px 15px;
    border-radius: 5px;
    margin: 15px 0;
  }

  /* 표 스타일 */
  table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
  }
  th, td {
    border: 1px solid #dee2e6;
    padding: 10px 12px;
    text-align: left;
  }
  th {
    background-color: #f9a825;
    color: white;
  }
  tr:nth-child(even) {
    background-color: #f8f9fa;
  }

---

# ✨ 코드 블록 테스트

```json
{
  "role": "assistant",
  "content": "네, 알람을 설정하였습니다. 현재 시간은 14:23이며, 1시간 후인 15:23에 알람이 울리도록 설정되었습니다.",
  "tool_calls": null
}


## 🏗️ 코드 예시

```java
// UserController.java
@RestController
@RequestMapping("/api/users")
public class UserController {

  @GetMapping("/{id}")
  public ResponseEntity<User> getUser(@PathVariable Long id) {
    // TODO: 유저 조회 로직 추가
    return ResponseEntity.ok(new User(id, "홍길동"));
  }
}


# Tool Calling 체인 흐름 가이드

---

## 개요
이 문서는 Spring AI의 Tool Calling 메커니즘이 어떻게 작동하는지, 질문부터 최종 응답까지의 전체 흐름을 단계별로 설명합니다.

**예시 질문**: "지금부터 1시간 뒤 알람이 울리도록 설정해줘"

---

## 전체 아키텍처

```
[Controller] → [Service] → [ChatClient] → [LLM] → [Tool Execution] → [LLM] → [Response]
                      └────────────────────────────────┘
                              Tool Registration
```

---

## 단계별 상세 흐름

### 1단계: Controller에서 요청 수신

**파일**: `DateTimeController.java`

```java
@PostMapping(value = "/date-time-tools", ...)
public String dateTimeTools(@RequestParam("question") String question) {
    String answer = dateTimeService.chat(question);
    return answer;
}
```

**데이터 흐름:**
- 클라이언트로부터 질문 수신: `"지금부터 1시간 뒤 알람이 울리도록 설정해줘"`
- DateTimeService로 전달

---

### 2단계: Service에서 ChatClient 구성

**파일**: `DateTimeService.java`

```java
public String chat(String question) {
    String answer = this.chatClient.prompt()
        .user(question)
        .tools(dateTimeTools)
        .call()
        .content();
    return answer;
}
```

**중요 작업:**
- `dateTimeTools` 객체를 등록하여 LLM에 사용 가능한 도구 정보 제공
- 사용자 질문(`question`)을 추가
- `.call()`로 LLM 호출

---

### 3단계: Tool 정보 수집 및 시스템 프롬프트 구성

Spring AI는 등록된 Tool들을 분석하여 다음과 같은 Tool 정보를 생성합니다:

#### 3.1 Tool 등록 정보 (내부 처리)

**등록된 Tools:**
1. `getCurrentDateTime`
   - Description: "현재 날짜와 시간 정보를 제공합니다."
   - Parameters: 없음
   - Return Type: String

2. `setAlarm`
   - Description: "지정된 시간에 알람을 설정합니다."
   - Parameters:
     - `time` (String, required): "ISO-8601 형식의 시간"
   - Return Type: void

#### 3.2 LLM에 전송되는 실제 메시지 구조

```json
{
  "messages": [
    {
      "role": "user",
      "content": "지금부터 1시간 뒤 알람이 울리도록 설정해줘"
    }
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "getCurrentDateTime",
        "description": "현재 날짜와 시간 정보를 제공합니다.",
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
        "name": "setAlarm",
        "description": "지정된 시간에 알람을 설정합니다.",
        "parameters": {
          "type": "object",
          "properties": {
            "time": {
              "type": "string",
              "description": "ISO-8601 형식의 시간"
            }
          },
          "required": ["time"]
        }
      }
    }
  ]
}
```

**핵심:**
- LLM은 사용 가능한 모든 도구의 이름, 설명, 파라미터 구조를 받습니다
- LLM이 스스로 어떤 도구를 호출할지, 어떤 인자로 호출할지 결정합니다

---

### 4단계: LLM의 Tool 호출 결정

LLM은 질문을 분석하고 다음과 같이 추론합니다:

1. "지금부터 1시간 뒤" → 현재 시간이 필요
2. "알람 설정" → 알람 설정 도구 필요

#### 4.1 LLM의 첫 번째 응답 (Tool Call 요청)

```json
{
  "role": "assistant",
  "content": null,
  "tool_calls": [
    {
      "id": "call_abc123",
      "type": "function",
      "function": {
        "name": "getCurrentDateTime",
        "arguments": "{}"
      }
    }
  ]
}
```

**LLM의 사고 과정:**
- 현재 시간을 알아야 1시간 후를 계산할 수 있다
- `getCurrentDateTime()` 도구를 먼저 호출해야 한다

---

### 5단계: Tool 실행

Spring AI는 `tool_calls`을 감지하고 실제 Java 메서드를 호출합니다.

#### 5.1 getCurrentDateTime 실행

**코드:**
```java
@Tool(description = "현재 날짜와 시간 정보를 제공합니다.")
public String getCurrentDateTime() {
    String nowTime = LocalDateTime.now()
            .atZone(LocaleContextHolder.getTimeZone().toZoneId())
            .toString();
    log.info("현재 시간: {}", nowTime);
    return nowTime;
}
```

**실행 결과:**
- 예시 반환 값: `"2025-01-15T14:23:45+09:00[Asia/Seoul]"`
- 로그 출력: "현재 시간: 2025-01-15T14:23:45+09:00[Asia/Seoul]"

#### 5.2 Tool 결과를 LLM에 다시 전송

Spring AI는 Tool 실행 결과를 다시 LLM에게 전송합니다:

```json
{
  "messages": [
    {
      "role": "user",
      "content": "지금부터 1시간 뒤 알람이 울리도록 설정해줘"
    },
    {
      "role": "assistant",
      "content": null,
      "tool_calls": [
        {
          "id": "call_abc123",
          "type": "function",
          "function": {
            "name": "getCurrentDateTime",
            "arguments": "{}"
          }
        }
      ]
    },
    {
      "role": "tool",
      "name": "getCurrentDateTime",
      "tool_call_id": "call_abc123",
      "content": "2025-01-15T14:23:45+09:00[Asia/Seoul]"
    }
  ]
}
```

---

### 6단계: LLM의 두 번째 응답 (setAlarm Tool Call)

LLM은 현재 시간(`2025-01-15T14:23:45+09:00`)을 받고 1시간 후를 계산합니다:

**계산 결과:** `2025-01-15T15:23:45+09:00`

#### 6.1 LLM의 두 번째 Tool Call

```json
{
  "role": "assistant",
  "content": null,
  "tool_calls": [
    {
      "id": "call_xyz789",
      "type": "function",
      "function": {
        "name": "setAlarm",
        "arguments": "{\"time\": \"2025-01-15T15:23:45+09:00\"}"
      }
    }
  ]
}
```

**LLM의 사고 과정:**
- 현재 시간: 14:23:45
- 1시간 후: 15:23:45
- setAlarm 도구를 호출해야 함
- ISO-8601 형식으로 시간 전달

---

### 7단계: setAlarm Tool 실행

#### 7.1 setAlarm 메서드 실행

**코드:**
```java
@Tool(description = "지정된 시간에 알람을 설정합니다.")
public void setAlarm(
    @ToolParam(description = "ISO-8601 형식의 시간", required = true) 
    String time) {
    // "T24:" 패턴 처리 로직
    if (time.contains("T24:")) {
      // 24:XX:XX → 00:XX:XX 변환 및 날짜 증가
    }
    LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
    log.info("알람 설정 시간: " + alarmTime);
}
```

**실행:**
- 입력: `"2025-01-15T15:23:45+09:00"`
- 파싱 성공: `LocalDateTime(2025-01-15T15:23:45)`
- 로그 출력: "알람 설정 시간: 2025-01-15T15:23:45"

**주의:** 
- 파라미터가 `"2025-01-15T24:12:29+09:00"`인 경우, `T24:` 패턴이 감지되어 자동으로 다음 날 `00:12:29`로 변환됩니다

#### 7.2 Tool 결과 전송

```json
{
  "messages": [
    {
      "role": "user",
      "content": "지금부터 1시간 뒤 알람이 울리도록 설정해줘"
    },
    {
      "role": "assistant",
      "content": null,
      "tool_calls": [...]
    },
    {
      "role": "tool",
      "name": "getCurrentDateTime",
      "tool_call_id": "call_abc123",
      "content": "2025-01-15T14:23:45+09:00[Asia/Seoul]"
    },
    {
      "role": "assistant",
      "content": null,
      "tool_calls": [
        {
          "id": "call_xyz789",
          "function": {
            "name": "setAlarm",
            "arguments": "{\"time\": \"2025-01-15T15:23:45+09:00\"}"
          }
        }
      ]
    },
    {
      "role": "tool",
      "name": "setAlarm",
      "tool_call_id": "call_xyz789",
      "content": "null"
    }
  ]
}
```

**주목할 점:**
- `setAlarm`의 반환 타입이 `void`이므로 content가 `null`입니다
- LLM은 이 정보를 받고 최종 응답을 생성할 수 있습니다

---

### 8단계: LLM의 최종 응답 생성

LLM은 모든 Tool 실행이 완료되었고 알람이 성공적으로 설정되었음을 확인합니다.

#### 8.1 LLM의 최종 응답

```json
{
  "role": "assistant",
  "content": "네, 알람을 설정해드렸습니다. 현재 시간은 14:23이며, 1시간 후인 15:23에 알람이 울리도록 설정되었습니다.",
  "tool_calls": null
}
```

**LLM의 응답 이유:**
- Tool 호출이 더 이상 필요 없음 (모든 작업 완료)
- 사용자에게 친숙한 형식으로 결과를 정리하여 응답
- 현재 시간과 알람 시간을 명확히 언급

---

### 9단계: Controller로 응답 반환

```java
public String dateTimeTools(@RequestParam("question") String question) {
    String answer = dateTimeService.chat(question);
    return answer;  // "네, 알람을 설정해드렸습니다. ..."
}
```

**최종 반환 값:**
```
"네, 알람을 설정해드렸습니다. 현재 시간은 14:23이며, 1시간 후인 15:23에 알람이 울리도록 설정되었습니다."
```

---

## 핵심 개념 정리

### 1. Tool 등록
- `@Component`로 등록된 Tool 클래스
- `@Tool` 어노테이션으로 메서드를 도구로 표시
- Spring AI가 자동으로 Tool 정보를 추출하여 LLM에 제공

### 2. Tool Calling 루프
```
LLM → Tool Call 결정 → Spring AI가 Tool 실행 → 결과 반환 → LLM → (최종 응답 or 추가 Tool Call)
```

### 3. 메시지 누적
- Tool Call과 결과가 대화 히스토리에 계속 추가됨
- LLM은 전체 컨텍스트를 바탕으로 다음 행동 결정

### 4. 멀티 턴 대화 가능
- 여러 Tool을 순차적으로 호출 가능
- 이전 Tool 결과를 기반으로 다음 Tool 호출 가능

---

## 실제 로그 예시

```
2025-01-15 14:23:45.123  INFO  --- [main] c.e.d.d.DateTimeTools : 현재 시간: 2025-01-15T14:23:45+09:00[Asia/henderson/Seoul]
2025-01-15 14:23:45.456  INFO  --- [main] c.e.d.d.DateTimeTools : 알람 설정 시간: 2025-01-15T15:23:45
```

---

## 오류 처리 예시

### Case 1: 잘못된 시간 형식
LLM이 `"2025-01-15T15:23"` (초 없음)을 전달하면:
- `LocalDateTime.parse()` 실패
- 예외 발생 → Spring AI가 LLM에 오류 메시지 전달
- LLM이 올바른 형식으로 재시도할 수 있음

### Case 2: 24시 이상 시간
LLM이 `"2025-01-15T24:12:29+09:00"`를 전달하면:
- `T24:` 패턴 감지
- 자동으로 `2025-01-16T00:12:29+09:00`로 변환
- 다음 날 00:12:29에 알람 설정

---

## Spring AI의 자동화된 기능

1. **Tool 정보 추출**: 어노테이션 기반 자동 추출
2. **Function Calling 프로토콜 변환**: Java 메서드 ↔ LLM Function Call
3. **타입 변환**: JSON ↔ Java Object 자동 매핑
4. **에러 처리**: 예외 발생 시 LLM에 적절한 메시지 전달
5. **멀티 턴 관리**: Tool Call과 결과를 자동으로 대화 히스토리에 추가

---

## 참고 파일

- `DateTimeController.java`: HTTP 요청 수신
- `DateTimeService.java`: ChatClient 구성 및 호출
- `DateTimeTools.java`: 실제 Tool 구현체
- `application.properties`: LLM API 설정

---

## 마무리

Spring AI의 Tool Calling은 LLM과 실제 시스템을 연결하는 강력한 메커니즘입니다. 개발자는 복잡한 LLM 통신 프로토콜을 신경 쓰지 않고, `@Tool` 어노테이션만으로 자연어 처리와 도구 실행을 통합할 수 있습니다.

