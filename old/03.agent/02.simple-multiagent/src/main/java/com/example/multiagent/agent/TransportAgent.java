package com.example.multiagent.agent;

import com.example.multiagent.tool.TransportTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

// 교통편 Agent: 사용자의 요청 프롬프트에 따라 Tool이 제공한 교통편 안에서 답한다
@Component
public class TransportAgent {

    private static final Logger log = LoggerFactory.getLogger(TransportAgent.class);

    private static final String SYSTEM_PROMPT = """
            당신은 출장 교통편 Agent이다.
            반드시 getTransportationOptions Tool을 호출해서 교통편 목록을 조회한 뒤 답한다.
            제약:
            - Tool이 제공한 교통편 데이터만 근거로 답한다. 없는 교통편을 지어내지 않는다.
            - 사용자 요청에 선택 기준(빠른 것, 저렴한 것, 개수 등)이 있으면 그 기준을 최우선으로 따른다.
            - 요청에 별도 기준이 없으면 기본 규칙을 적용한다:
              왕복 비용(편도 x 2)이 전체 예산의 30% 이내인 교통편 중 가장 빠른 것 1개를 선택하고,
              해당되는 것이 없으면 가장 저렴한 것을 선택한다.
            - 답변에는 교통편 이름, 비용(편도/왕복), 추천 이유를 포함하고 간결하게 답한다.
            """;

    private final ChatClient chatClient;
    private final TransportTool transportTool;

    public TransportAgent(ChatClient.Builder builder, TransportTool transportTool) {
        this.chatClient = builder.build();
        this.transportTool = transportTool;
    }

    public String run(String destination, int budget, String request) {
        log.info("[Agent 시작] TransportAgent - 요청: {}", request);
        String result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        출장 지역: %s, 전체 예산: %,d원

                        [요청]
                        %s
                        """.formatted(destination, budget, request))
                .tools(transportTool)
                .call()
                .content();
        log.info("[Agent 완료] TransportAgent\n{}", result);
        return result;
    }
}
