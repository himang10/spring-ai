package com.example.multiagent.orchestrator;

// [목적] 하위 Agent 호출 1건의 기록을 담는 데이터 그릇 (로직 없음)
//   - agentName: 호출된 하위 Agent 이름, request: Orchestrator가 작성한 요청 프롬프트, result: 그 응답
// [필요성] Orchestrator의 하위 Agent 호출은 LLM Tool Calling 내부에서 일어나 밖에서는 최종 답변만 보인다.
//   "요청을 어떻게 분해해서 누구를 어떤 순서로 불렀는가"를 UI에 보여주기 위해
//   AgentTools가 호출마다 한 건씩 기록하고, Controller가 꺼내 화면에 순서대로 표시한다.
public record AgentStep(String agentName, String request, String result) {
}
