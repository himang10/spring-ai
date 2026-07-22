# 02.simple-multiagent — Orchestration 기반 Multi-Agent 샘플

01.simple-agent의 개별 Agent(교통편 / 숙소 / 식당)를 그대로 재사용하면서,
**Orchestrator Agent** 하나를 추가해 Multi-Agent로 확장한 교육용 샘플이다.

## 1. 어떤 Multi-Agent인가?

이 샘플은 **Orchestrator-Workers 패턴**이다.

- **Orchestrator Agent(상위)**: 직접 일하지 않는다. 사용자의 요청 프롬프트를 읽고
  하위 Agent별 요청 프롬프트를 직접 작성해서 호출한 뒤, 결과를 조합해 최종 출장 계획을 만든다.
- **Worker Agent(하위)**: 01.simple-agent와 동일한 3개의 Agent.
  각자 자신의 Tool 데이터 안에서 전달받은 요청 프롬프트에 맞게 답한다.

```
사용자 입력 (지역, 날짜, 예산) + 요청 프롬프트
  예) "이동은 빠르게, 숙소는 회사 근처, 회식용 한식당 추천"
        │
        ▼
┌─────────────────────┐
│ Orchestrator Agent  │  ← 요청을 분해해서 하위 Agent별 요청 프롬프트를 작성
└─────────────────────┘
   │ "가장 빠른              │ "회사에서 가까운        │ "회식용 한식당을
   │  교통편 추천해줘"        │  숙소 추천해줘"         │  추천해줘"
   ▼                        ▼                       ▼
┌──────────────┐      ┌──────────────┐      ┌────────────────┐
│ TransportAgent│      │ HotelAgent   │      │ RestaurantAgent │
│  (교통편)     │      │  (숙소)       │      │  (식당)         │
└──────────────┘      └──────────────┘      └────────────────┘
   │ @Tool                  │ @Tool                 │ @Tool
   ▼                        ▼                       ▼
TransportAgent.json   HotelAgent.json       RestaurantAgent.json
```

핵심 아이디어는 두 가지다.

1. **Agent-as-Tool**: 하위 Agent를 `@Tool`로 감싸서 상위 Agent가 호출한다.
   "Tool을 쓰는 Agent"를 그대로 "Agent를 쓰는 Agent"로 한 단계 확장한 것이다.
2. **요청 프롬프트 분해**: 사용자는 자연어 요청 하나만 작성하고,
   하위 Agent별 요청 프롬프트는 Orchestrator LLM이 스스로 작성한다.
   UI에서 각 하위 Agent 카드에 "Orchestrator의 요청"이 표시되어 분해 과정을 확인할 수 있다.

## 2. 01.simple-agent 대비 변경 사항

| 구분 | 01.simple-agent | 02.simple-multiagent |
|------|-----------------|----------------------|
| Worker Agent 3개 (`agent/`) | 있음 | **변경 없음** (패키지명만 변경) |
| Tool 3개 (`tool/`) | 있음 | **변경 없음** (패키지명만 변경) |
| 가상 데이터 (`data/*.json`) | 있음 | **변경 없음** |
| Agent 선택 | **사용자가 UI에서 직접** Agent를 선택하고 요청 프롬프트를 작성 | **Orchestrator가** 요청을 분해해서 세 Agent를 모두 호출 |
| 요청 프롬프트 작성자 | 사용자 | 사용자는 전체 요청 1개만, Agent별 요청은 **Orchestrator LLM이 작성** |
| `orchestrator/AgentTools.java` | 없음 | **신규**: 하위 Agent를 `@Tool`로 래핑 |
| `orchestrator/OrchestratorAgent.java` | 없음 | **신규**: 요청 분해와 하위 Agent 호출을 총괄 |
| `TripController` | 선택된 Agent 하나를 실행 | Orchestrator 하나만 호출 |

바뀐 것은 "누가 Agent를 고르고 요청 프롬프트를 작성하는가"이다.

- 01: **사용자가** Agent를 선택하고 요청을 직접 작성한다. (수동 라우팅)
- 02: **Orchestrator LLM이** 전체 요청을 분해해서 Agent별 요청을 작성하고 호출한다. (자동 오케스트레이션)

## 3. 실행 흐름

1. 사용자가 UI에서 지역/날짜/예산과 전체 요청 프롬프트를 입력한다.
2. `TripController`는 `OrchestratorAgent.run()` 하나만 호출한다.
3. Orchestrator는 요청을 분해해서 하위 Agent별 요청 프롬프트를 작성하고,
   `callTransportAgent` / `callHotelAgent` / `callRestaurantAgent`를 Tool Calling으로 호출한다.
4. 각 하위 Agent는 자신의 `@Tool`로 JSON 가상 데이터를 조회하고 전달받은 요청에 맞게 답한다.
5. Orchestrator가 세 결과를 조합해 `[최종 출장 계획]`을 만든다.
6. UI에는 하위 Agent별 "Orchestrator의 요청 + 응답"과 최종 계획이 함께 표시된다.

콘솔 로그에서 `[Orchestrator 시작]` → `[Orchestrator -> XxxAgent] 요청: ...` → `[Agent 시작]` → `[Tool 호출]` 순서로 흐름을 확인할 수 있다.

## 4. 실행 방법

```bash
export OPENAI_API_KEY=<your-key>
mvn spring-boot:run
```

- http://localhost:8081 접속 (01.simple-agent와 동시 실행할 수 있도록 8081 포트 사용)
- 출장 정보와 요청 프롬프트 입력 후 "Multi-Agent 실행" 클릭

요청 프롬프트 예:
- "이동은 최대한 빠르게 하고, 숙소는 회사에서 가까운 곳으로, 저녁 회식용 한식당도 추천해줘"
- "전체적으로 비용을 최대한 아끼는 방향으로 계획을 잡아줘"

## 5. Multi-Agent로 확장하는 방법 (가이드)

기존 단일 Agent 프로젝트를 Multi-Agent로 확장할 때의 순서:

1. **Worker Agent는 손대지 않는다.**
   각 Agent가 "입력 + 요청 프롬프트 → 결과 텍스트" 형태의 독립 메서드(`run(...)`)를 갖고 있으면 그대로 재사용할 수 있다.
2. **Agent를 Tool로 감싼다.** (`AgentTools.java`)
   하위 Agent의 `run()`을 호출하는 메서드에 `@Tool` + 설명을 붙인다.
   요청 프롬프트를 파라미터(`@ToolParam`)로 열어두면 Orchestrator가 Agent별 요청을 작성해서 전달할 수 있다.
3. **Orchestrator Agent를 만든다.** (`OrchestratorAgent.java`)
   시스템 프롬프트에 요청 분해 방법, 호출할 하위 Agent, 최종 출력 형식을 정의하고
   `.tools(agentTools)`로 하위 Agent Tool을 연결한다.
4. **Controller를 단순화한다.**
   Agent 선택/분기 코드를 지우고 Orchestrator 호출 하나만 남긴다.

### 새 Worker Agent 추가 예 (관광지 추천 Agent)

1. `data/AttractionAgent.json` 가상 데이터 생성
2. `tool/AttractionTool.java`에 `@Tool getAttractionOptions(destination)` 작성
3. `agent/AttractionAgent.java`에 역할 프롬프트와 `run(destination, budget, request)` 작성
4. `AgentTools.java`에 `callAttractionAgent(...)` `@Tool` 메서드 추가
5. `OrchestratorAgent`의 시스템 프롬프트에 호출 단계 한 줄 추가

Worker가 늘어나도 Controller와 UI 구조는 바뀌지 않는다는 점이 Orchestration 방식의 장점이다.

## 6. 프로젝트 구조

```
src/main/java/com/example/multiagent/
├── MultiAgentApplication.java
├── agent/                       # 01.simple-agent와 동일 (변경 없음)
│   ├── TransportAgent.java
│   ├── HotelAgent.java
│   └── RestaurantAgent.java
├── tool/                        # 01.simple-agent와 동일 (변경 없음)
│   ├── TransportTool.java
│   ├── HotelTool.java
│   └── RestaurantTool.java
├── orchestrator/                # Multi-Agent 확장으로 신규 추가
│   ├── OrchestratorAgent.java   # 요청 분해와 하위 Agent 호출을 총괄하는 상위 Agent
│   ├── AgentTools.java          # 하위 Agent를 @Tool로 래핑 (Agent-as-Tool)
│   └── AgentStep.java           # 하위 Agent별 요청/응답 기록 (UI 표시용)
└── controller/
    └── TripController.java      # Orchestrator 하나만 호출하도록 단순화
```
