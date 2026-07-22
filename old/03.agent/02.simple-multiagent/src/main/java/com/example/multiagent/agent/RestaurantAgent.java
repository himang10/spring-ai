package com.example.multiagent.agent;

import com.example.multiagent.tool.RestaurantTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

// 식당 Agent: 사용자의 요청 프롬프트에 따라 Tool이 제공한 식당 안에서 답한다
@Component
public class RestaurantAgent {

    private static final Logger log = LoggerFactory.getLogger(RestaurantAgent.class);

    private static final String SYSTEM_PROMPT = """
            당신은 출장 식당 추천 Agent이다.
            반드시 getRestaurantOptions Tool을 호출해서 식당 목록을 조회한 뒤 답한다.
            제약:
            - Tool이 제공한 식당 데이터만 근거로 답한다. 없는 식당을 지어내지 않는다.
            - 사용자 요청에 기준(음식 종류, 가격대, 거리, 개수 등)이 있으면 그 기준을 최우선으로 따른다.
            - 요청에 별도 기준이 없으면 회사와 가까운 순으로 2곳을 추천한다.
            - 답변에는 식당 이름, 음식 종류, 1인 가격, 회사까지 거리, 추천 이유를 포함하고 간결하게 답한다.
            """;

    private final ChatClient chatClient;
    private final RestaurantTool restaurantTool;

    public RestaurantAgent(ChatClient.Builder builder, RestaurantTool restaurantTool) {
        this.chatClient = builder.build();
        this.restaurantTool = restaurantTool;
    }

    public String run(String destination, int budget, String request) {
        log.info("[Agent 시작] RestaurantAgent - 요청: {}", request);
        String result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        출장 지역: %s, 전체 예산: %,d원

                        [요청]
                        %s
                        """.formatted(destination, budget, request))
                .tools(restaurantTool)
                .call()
                .content();
        log.info("[Agent 완료] RestaurantAgent\n{}", result);
        return result;
    }
}
