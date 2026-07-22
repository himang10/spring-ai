# ConceptDesign - 02.simple-multiagent

## 핵심 설계: Agent-as-Tool + 요청 프롬프트 분해

01.simple-agent에서 Agent가 @Tool(데이터 조회)을 사용했던 것을 그대로 한 단계 확장해,
하위 Agent 자체를 @Tool로 감싸서 상위 Orchestrator Agent가 호출하는 구조입니다.
사용자는 전체 요청 프롬프트 하나만 작성하고, 하위 Agent별 요청 프롬프트는
Orchestrator LLM이 스스로 분해해서 작성합니다.

## 변경 없이 재사용한 것

01.simple-agent의 Agent 3개, Tool 3개, data/*.json 가상 데이터를
패키지명(`com.example.simpleagent` → `com.example.multiagent`)만 바꿔 그대로 복사했습니다.
내용(로직, 프롬프트, 데이터)은 한 글자도 바뀌지 않았습니다.

| 구분 | 원본 (01.simple-agent) | 복사본 (02.simple-multiagent) |
|------|--------------------------|-------------------------------|
| Agent | `agent/TransportAgent.java` | `agent/TransportAgent.java` |
| Agent | `agent/HotelAgent.java` | `agent/HotelAgent.java` |
| Agent | `agent/RestaurantAgent.java` | `agent/RestaurantAgent.java` |
| Tool | `tool/TransportTool.java` | `tool/TransportTool.java` |
| Tool | `tool/HotelTool.java` | `tool/HotelTool.java` |
| Tool | `tool/RestaurantTool.java` | `tool/RestaurantTool.java` |
| 가상 데이터 | `resources/data/TransportAgent.json` | `resources/data/TransportAgent.json` |
| 가상 데이터 | `resources/data/HotelAgent.json` | `resources/data/HotelAgent.json` |
| 가상 데이터 | `resources/data/RestaurantAgent.json` | `resources/data/RestaurantAgent.json` |

반대로 아래 파일들은 재사용 대상이 아니라 각 프로젝트에 새로 작성되었습니다. 다만 성격이 다릅니다.

**단순히 프로젝트별로 달라야 해서 다른 파일** (내용에 학습 포인트 없음)
- `SimpleAgentApplication.java` / `MultiAgentApplication.java` (Boot 진입점, 프로젝트별 클래스명 필요)
- `application.properties` (포트가 8080 vs 8081로 다름, 동시 실행 목적)

**의도적으로 다시 설계되어 이 샘플의 핵심 학습 포인트가 된 파일**
- `controller/TripController.java` — Agent 선택/요청 책임이 Orchestrator로 옮겨간 결과. 자세한 비교는 아래 "단순화된 것" 참고
- `templates/index.html` — Orchestrator가 작성한 하위 Agent별 요청 프롬프트와 응답(`steps`)을 순서대로 보여주기 위해 구조 추가

## 신규 추가한 것 (orchestrator 패키지)

- **AgentTools.java** — 하위 Agent의 run()을 callTransportAgent, callHotelAgent, callRestaurantAgent라는
  @Tool로 래핑. 요청 프롬프트를 @ToolParam으로 열어두어 Orchestrator가 Agent별 요청을 작성해서 전달합니다.
  실행 기록(AgentStep.java: Agent명 + Orchestrator의 요청 + 응답)도 남겨 UI에 순서대로 표시합니다.
- **OrchestratorAgent.java** — 직접 일하지 않고 사용자 요청을 분해해서 하위 Agent별 요청 프롬프트를 작성하고,
  호출 결과를 조합해 [최종 출장 계획]을 만듭니다.

## 단순화된 것

TripController.java를 실제로 비교하면 다음이 구체적으로 달라집니다.

**의존성 (필드/생성자)**
- 01: `TransportAgent`, `HotelAgent`, `RestaurantAgent` 3개를 각각 주입받음
- 02: `OrchestratorAgent`, `AgentTools`(실행 기록 조회용) 2개만 주입받음
  → Controller가 개별 Worker Agent 타입을 더 이상 알 필요가 없음

**Agent 선택과 요청 전달**
- 01: 사용자가 UI에서 Agent를 선택하면 Controller가 `switch (agentType)`으로 분기해서
  해당 Agent의 `run()`을 호출 (Agent가 늘면 분기도 늘어남)
- 02: 분기 없이 `orchestratorAgent.run(destination, nights, budget, request)` **호출 1줄**
  → 어떤 Agent를 어떤 요청 프롬프트로 호출할지는 Orchestrator LLM이 결정

**Model에 담는 결과 데이터**
- 01: 선택된 Agent 하나의 `agentName`, `result`를 개별 attribute로 추가
- 02: `steps`(하위 Agent별 요청/응답 기록 리스트) + `finalPlan` 2개만 추가
  → Worker Agent가 늘어나도 Controller 코드는 변경되지 않음 (AgentTools 안에서만 기록됨)

결과적으로 "Agent 선택 분기, Agent별 요청 프롬프트 작성, 결과 조합"이라는 책임이
사용자와 Controller에서 사라지고 Orchestrator/AgentTools 쪽으로 옮겨간 것이 이 샘플의 학습 포인트입니다.

## README

README.md에 어떤 Multi-Agent 패턴인지(구조 다이어그램 포함), 01 대비 변경 사항 비교표,
실행 흐름, Multi-Agent 확장 가이드(새 Worker Agent 추가 절차 예시 포함)를 작성했습니다.

## UI

index.html에서 사용자는 출장 정보와 전체 요청 프롬프트 하나만 입력합니다.
실행 결과에는 하위 Agent별로 "Orchestrator의 요청"(Orchestrator가 분해해서 작성한 프롬프트)과
응답이 호출 순서대로 카드로 표시되고, 마지막에 Orchestrator의 최종 계획이 강조 카드로 표시됩니다.

## 실행 방법

```bash
cd 02.simple-multiagent
export OPENAI_API_KEY=<키>
mvn spring-boot:run
```

포트는 8081이라 01.simple-agent(8080)와 동시에 띄워
단일 Agent 방식과 Multi-Agent 방식을 비교해볼 수 있습니다.
