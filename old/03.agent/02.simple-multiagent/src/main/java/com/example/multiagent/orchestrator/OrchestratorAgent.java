package com.example.multiagent.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

// Orchestrator Agent: 사용자 요청을 분해해서 하위 Agent별 요청 프롬프트를 작성하고 결과를 조합한다
@Component
public class OrchestratorAgent {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorAgent.class);

    private static final String SYSTEM_PROMPT = """
            당신은 출장 계획을 총괄하는 Orchestrator Agent이다.
            직접 교통편/숙소/식당을 정하지 않고, 반드시 하위 Agent(Tool)를 호출해서 결과를 조합한다.
            동작 방법:
            1. 사용자의 요청을 읽고, 교통편/숙소/식당 각각에 대해 하위 Agent에게 보낼 요청 프롬프트를 직접 작성한다.
               예) 사용자가 "이동은 빠르게, 숙소는 조용한 곳"을 원하면
                   교통편 Agent에는 "가장 빠른 교통편을 추천해줘",
                   숙소 Agent에는 "조용하게 지낼 수 있는 숙소를 추천해줘" 같은 요청을 작성한다.
            2. 사용자의 요청에 특정 기준이 없는 영역은 "예산에 맞게 추천해줘"라는 기본 요청 프롬프트를 작성한다.
            3. callTransportAgent, callHotelAgent, callRestaurantAgent 를 각각 1회씩 호출한다.
            제약:
            - 하위 Agent의 답변 내용을 임의로 바꾸지 않고 그대로 조합한다.
            아래 형식으로만 최종 답변한다.
            [최종 출장 계획]
            교통편: (추천 교통편과 왕복 비용)
            숙소: (추천 숙소와 총 숙박비)
            식당: (추천 식당)
            총 예상 비용: (교통 왕복 + 총 숙박비)원 (식비 별도)
            요약: (사용자 요청을 어떻게 반영했는지 한두 문장)
            """;

    private final ChatClient chatClient;
    private final AgentTools agentTools;

    public OrchestratorAgent(ChatClient.Builder builder, AgentTools agentTools) {
        this.chatClient = builder.build();
        this.agentTools = agentTools;
    }

    public String run(String destination, long nights, int budget, String request) {
        log.info("[Orchestrator 시작] 사용자 요청: {}", request);
        String result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        출장 지역: %s, 숙박 일수: %d박, 전체 예산: %,d원

                        [사용자 요청]
                        %s
                        """.formatted(destination, nights, budget, request))
                .tools(agentTools)
                .call()
                .content();
        log.info("[Orchestrator 완료]\n{}", result);
        return result;
    }
}
