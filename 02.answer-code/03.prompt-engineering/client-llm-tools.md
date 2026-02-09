# LLM Tool API 요청/응답 구조

이 문서는 LLM(Language Model)에 Tool 기능을 제공하기 위한 HTTP API의 요청과 응답 데이터 구조를 설명합니다.

## 목차
- [개요](#개요)
- [Tool Request 구조](#tool-request-구조)
- [Tool Response 구조](#tool-response-구조)
- [Tool 정의 예시](#tool-정의-예시)

---

## 개요

LLM Tool API는 클라이언트가 LLM에게 외부 함수(Tool)를 호출할 수 있는 능력을 제공합니다. 이를 통해 LLM은 실시간 정보 조회, 데이터 처리 등의 작업을 수행할 수 있습니다.

## Tool Request 구조

### HTTP Body 형식

```json
{
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "tool_choice": "auto",
  "messages": [...],
  "tools": [...]
}
```

### 필드 설명

#### 기본 설정 필드

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| `model` | String | 사용할 LLM 모델 이름 | `"gpt-4o-mini"` |
| `temperature` | Number | 응답의 창의성 조절 (0.0~2.0) | `0.7` |
| `tool_choice` | String | Tool 사용 방식 (`auto`, `none`, `required`) | `"auto"` |

#### messages 배열

대화 컨텍스트를 정의하는 메시지 배열입니다.

```json
"messages": [
  {
    "role": "system",
    "content": "당신은 SKALA AI Assistant입니다."
  },
  {
    "role": "user",
    "content": "현재 위치, 날씨 정보, 상황을 알려주세요"
  }
]
```

**메시지 역할(role) 타입:**
- `system`: 시스템 프롬프트 (AI의 역할/동작 정의)
- `user`: 사용자 입력
- `assistant`: AI 응답
- `tool`: Tool 실행 결과

#### tools 배열

사용 가능한 Tool 함수들의 정의를 포함합니다.

```json
"tools": [
  {
    "type": "function",
    "function": {
      "name": "getCurrentTime",
      "description": "현재 시간 정보를 가져옵니다.",
      "parameters": {
        "type": "object",
        "properties": {},
        "required": []
      }
    }
  }
]
```

**Tool 객체 구조:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | String | 항상 `"function"` |
| `function.name` | String | 호출할 함수 이름 |
| `function.description` | String | 함수 기능 설명 (LLM이 사용 시점을 판단) |
| `function.parameters` | Object | JSON Schema 형식의 매개변수 정의 |

---

## Tool Response 구조

### 일반 응답 (Tool 호출 없음)

```json
{
  "id": "chatcmpl-xxx",
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "안녕하세요! 무엇을 도와드릴까요?"
      },
      "finish_reason": "stop"
    }
  ]
}
```

### Tool 호출 응답

LLM이 Tool을 호출해야 한다고 판단한 경우:

```json
{
  "id": "chatcmpl-xxx",
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": null,
        "tool_calls": [
          {
            "id": "call_xxx",
            "type": "function",
            "function": {
              "name": "getCurrentTime",
              "arguments": "{}"
            }
          }
        ]
      },
      "finish_reason": "tool_calls"
    }
  ]
}
```

**tool_calls 필드:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | String | Tool 호출 고유 ID |
| `type` | String | `"function"` |
| `function.name` | String | 호출할 함수 이름 |
| `function.arguments` | String | JSON 문자열 형식의 인자 |

---

## Tool 정의 예시

### 전체 Request 예시

```json
{
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "tool_choice": "auto",
  "messages": [
    {
      "role": "system",
      "content": "당신은 SKALA AI Assistant입니다."
    },
    {
      "role": "user",
      "content": "현재 위치, 날씨 정보, 상황을 알려주세요"
    }
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "getCurrentTime",
        "description": "현재 시간 정보를 가져옵니다. 사용자가 시간, 몇 시, 시각 등을 물어볼 때 사용하세요.",
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
        "name": "getWeatherInfo",
        "description": "현재 날씨 정보를 가져옵니다. 사용자가 날씨, 기온, 온도 등을 물어볼 때 사용하세요.",
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
        "name": "getLocationInfo",
        "description": "현재 위치 정보를 가져옵니다. 사용자가 위치, 장소, 어디 등을 물어볼 때 사용하세요.",
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
        "name": "getStatusInfo",
        "description": "현재 상황 정보를 가져옵니다. 사용자가 교통, 상황, 상태 등을 물어볼 때 사용하세요.",
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
        "name": "getAllInfo",
        "description": "모든 정보(시각, 날씨, 위치, 상황)를 한 번에 가져옵니다. 사용자가 전체 정보, 모든 정보 등을 요청하거나 복합적인 질문을 할 때 사용하세요.",
        "parameters": {
          "type": "object",
          "properties": {},
          "required": []
        }
      }
    }
  ]
}
```

### Tool 실행 흐름 개요

1. **클라이언트 → LLM**: Tool 정의와 사용자 메시지 전송
2. **LLM 판단**: 사용자 요청 분석 후 필요한 Tool 선택
3. **LLM → 클라이언트**: `tool_calls` 포함한 응답 반환
4. **클라이언트**: Tool 함수 실행
5. **클라이언트 → LLM**: Tool 실행 결과를 새 메시지로 전송
6. **LLM → 클라이언트**: 최종 답변 생성

---

## 실제 Tool 호출 예시

사용자가 "현재 위치, 날씨 정보, 상황을 알려주세요"라고 요청했을 때의 전체 흐름을 단계별로 설명합니다.

### 1단계: 초기 요청 (클라이언트 → LLM)

클라이언트가 사용자 질문과 Tool 정의를 포함하여 LLM에 요청을 보냅니다.

```json
{
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "tool_choice": "auto",
  "messages": [
    {
      "role": "system",
      "content": "당신은 SKALA AI Assistant입니다."
    },
    {
      "role": "user",
      "content": "현재 위치, 날씨 정보, 상황을 알려주세요"
    }
  ],
  "tools": [...]
}
```

### 2단계: Tool 호출 지시 응답 (LLM → 클라이언트)

LLM은 질문에 답하기 위해 `getAllInfo` Tool이 필요하다고 판단하고, Tool 호출을 지시하는 응답을 반환합니다.

```json
{
  "id": "chatcmpl-abc123",
  "object": "chat.completion",
  "choices": [
    {
      "index": 0,
      "finish_reason": "tool_calls",
      "message": {
        "role": "assistant",
        "content": null,
        "tool_calls": [
          {
            "id": "call_001",
            "type": "function",
            "function": {
              "name": "getAllInfo",
              "arguments": "{}"
            }
          }
        ]
      }
    }
  ]
}
```

**핵심 포인트:**
- `finish_reason`: `"tool_calls"` - Tool 호출이 필요함을 나타냄
- `content`: `null` - 아직 최종 답변이 아님
- `tool_calls`: 실행할 Tool의 이름과 인자를 포함

### 3단계: Tool 실행 및 결과 전송 (클라이언트 → LLM)

클라이언트(Spring AI 애플리케이션)가 `getAllInfo()` 메서드를 실행하고, 그 결과를 `role: "tool"` 메시지로 추가하여 다시 LLM에 요청합니다.

**Tool 실행 결과:**
```
[전체 정보]
- 시각: 오전 11시 10분
- 날씨: 11월 겨울 날씨, 기온 5°C, 맑음
- 위치: 대한민국, 경기도 수원시 장안구 (북수원)
- 상황: 평일 오전 시간대로 출근 시간이 지나 도로 교통량은 보통 수준입니다...
```

**2차 요청 (이전 대화 + Tool 결과):**

```json
{
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "messages": [
    {
      "role": "system",
      "content": "당신은 SKALA AI Assistant입니다."
    },
    {
      "role": "user",
      "content": "현재 위치, 날씨 정보, 상황을 알려주세요"
    },
    {
      "role": "assistant",
      "content": null,
      "tool_calls": [
        {
          "id": "call_001",
          "type": "function",
          "function": {
            "name": "getAllInfo",
            "arguments": "{}"
          }
        }
      ]
    },
    {
      "role": "tool",
      "tool_call_id": "call_001",
      "content": "[전체 정보]\n- 시각: 오전 11시 10분\n- 날씨: 11월 겨울 날씨, 기온 5°C, 맑음\n- 위치: 대한민국, 경기도 수원시 장안구 (북수원)\n- 상황: 평일 오전 시간대로 출근 시간이 지나 도로 교통량은 보통 수준입니다. 주변 카페와 식당은 점심 준비 중이며, 인근 대학교는 수업이 진행 중입니다. 기온이 낮아 두꺼운 외투 착용이 권장됩니다."
    }
  ]
}
```

**ToolResponseMessage 구조:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `role` | String | 항상 `"tool"` |
| `tool_call_id` | String | 2단계에서 받은 Tool 호출 ID (`call_001`) |
| `content` | String | Tool 함수의 실행 결과 (문자열) |

### 4단계: 최종 답변 (LLM → 클라이언트)

LLM은 Tool 실행 결과를 분석하고, 사용자에게 자연어로 정리된 최종 답변을 제공합니다.

```json
{
  "id": "chatcmpl-def456",
  "object": "chat.completion",
  "choices": [
    {
      "index": 0,
      "finish_reason": "stop",
      "message": {
        "role": "assistant",
        "content": "요청하신 현재 정보입니다.\n\n- 위치: 대한민국 경기도 수원시 장안구(북수원)\n- 날씨: 11월 겨울 날씨로 기온 5°C, 맑음입니다.\n- 상황: 평일 오전으로 출근 시간대가 지나 도로 교통량은 보통 수준이며, 주변 카페/식당은 점심 준비 중입니다. 인근 대학교는 수업이 진행 중이고, 기온이 낮아 두꺼운 외투 착용이 권장됩니다."
      }
    }
  ]
}
```

**핵심 포인트:**
- `finish_reason`: `"stop"` - 대화 완료
- `content`: Tool 결과를 기반으로 생성된 자연어 답변
- LLM이 Tool 결과를 "Observation"으로 활용하여 최종 답변을 생성

### Tool 호출 흐름 다이어그램

```
[사용자 질문]
     ↓
[1. 클라이언트 → LLM] (messages + tools)
     ↓
[2. LLM → 클라이언트] (tool_calls 지시)
     ↓
[3. 클라이언트가 Tool 실행]
     ↓
[4. 클라이언트 → LLM] (role=tool 메시지 추가)
     ↓
[5. LLM → 클라이언트] (최종 답변)
     ↓
[사용자에게 응답 표시]
```

---

## 참고사항

- **tool_choice**: 
  - `auto`: LLM이 자동으로 Tool 사용 여부 결정
  - `none`: Tool 사용 금지
  - `required`: 반드시 Tool 사용
  
- **parameters**: JSON Schema 형식을 따르며, 복잡한 매개변수가 필요한 경우 `properties`와 `required` 배열에 정의

- **description**: LLM이 적절한 Tool을 선택하는 핵심 정보이므로 명확하고 구체적으로 작성
