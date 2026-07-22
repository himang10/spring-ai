package com.example.multiagent.agent;

import com.example.multiagent.tool.HotelTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

// 숙소 Agent: 사용자의 요청 프롬프트에 따라 Tool이 제공한 숙소 안에서 답한다
@Component
public class HotelAgent {

    private static final Logger log = LoggerFactory.getLogger(HotelAgent.class);

    private static final String SYSTEM_PROMPT = """
            당신은 출장 숙소 Agent이다.
            반드시 getHotelOptions Tool을 호출해서 숙소 목록을 조회한 뒤 답한다.
            제약:
            - Tool이 제공한 숙소 데이터만 근거로 답한다. 없는 숙소를 지어내지 않는다.
            - 사용자 요청에 선택 기준(가까운 곳, 저렴한 곳, 개수 등)이 있으면 그 기준을 최우선으로 따른다.
            - 요청에 별도 기준이 없으면 기본 규칙을 적용한다:
              총 숙박비(1박 가격 x 숙박 일수)가 전체 예산의 40% 이내인 숙소 중 회사와 가장 가까운 곳 1개를 선택하고,
              해당되는 것이 없으면 가장 저렴한 곳을 선택한다.
            - 답변에는 숙소 이름, 비용(1박/총 숙박비), 회사까지 거리, 추천 이유를 포함하고 간결하게 답한다.
            """;

    private final ChatClient chatClient;
    private final HotelTool hotelTool;

    public HotelAgent(ChatClient.Builder builder, HotelTool hotelTool) {
        this.chatClient = builder.build();
        this.hotelTool = hotelTool;
    }

    public String run(String destination, long nights, int budget, String request) {
        log.info("[Agent 시작] HotelAgent - 요청: {}", request);
        String result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        출장 지역: %s, 숙박 일수: %d박, 전체 예산: %,d원

                        [요청]
                        %s
                        """.formatted(destination, nights, budget, request))
                .tools(hotelTool)
                .call()
                .content();
        log.info("[Agent 완료] HotelAgent\n{}", result);
        return result;
    }
}
