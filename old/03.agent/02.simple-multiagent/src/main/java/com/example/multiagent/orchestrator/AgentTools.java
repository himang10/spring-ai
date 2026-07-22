package com.example.multiagent.orchestrator;

import com.example.multiagent.agent.HotelAgent;
import com.example.multiagent.agent.RestaurantAgent;
import com.example.multiagent.agent.TransportAgent;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

// 핵심: 하위 Agent를 Orchestrator가 호출할 수 있는 @Tool로 감싼다 (Agent-as-Tool 패턴)
// Orchestrator는 각 하위 Agent에게 보낼 요청 프롬프트를 직접 작성해서 전달한다
@Component
public class AgentTools {

    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);

    private final TransportAgent transportAgent;
    private final HotelAgent hotelAgent;
    private final RestaurantAgent restaurantAgent;

    // 하위 Agent 실행 기록 (교육용 단순 구현 - 동시 요청은 고려하지 않음)
    private final List<AgentStep> steps = new ArrayList<>();

    public AgentTools(TransportAgent transportAgent, HotelAgent hotelAgent, RestaurantAgent restaurantAgent) {
        this.transportAgent = transportAgent;
        this.hotelAgent = hotelAgent;
        this.restaurantAgent = restaurantAgent;
    }

    public void clearSteps() {
        steps.clear();
    }

    public List<AgentStep> getSteps() {
        return List.copyOf(steps);
    }

    @Tool(description = "교통편 Agent에게 요청 프롬프트를 보내서 교통편 추천을 받는다")
    public String callTransportAgent(
            @ToolParam(description = "출장 지역") String destination,
            @ToolParam(description = "전체 예산(원)") int budget,
            @ToolParam(description = "교통편 Agent에게 보낼 요청 프롬프트") String request) {
        log.info("[Orchestrator -> TransportAgent] 요청: {}", request);
        String result = transportAgent.run(destination, budget, request);
        steps.add(new AgentStep("교통편 Agent", request, result));
        return result;
    }

    @Tool(description = "숙소 Agent에게 요청 프롬프트를 보내서 숙소 추천을 받는다")
    public String callHotelAgent(
            @ToolParam(description = "출장 지역") String destination,
            @ToolParam(description = "숙박 일수(박)") long nights,
            @ToolParam(description = "전체 예산(원)") int budget,
            @ToolParam(description = "숙소 Agent에게 보낼 요청 프롬프트") String request) {
        log.info("[Orchestrator -> HotelAgent] 요청: {}", request);
        String result = hotelAgent.run(destination, nights, budget, request);
        steps.add(new AgentStep("숙소 Agent", request, result));
        return result;
    }

    @Tool(description = "식당 Agent에게 요청 프롬프트를 보내서 식당 추천을 받는다")
    public String callRestaurantAgent(
            @ToolParam(description = "출장 지역") String destination,
            @ToolParam(description = "전체 예산(원)") int budget,
            @ToolParam(description = "식당 Agent에게 보낼 요청 프롬프트") String request) {
        log.info("[Orchestrator -> RestaurantAgent] 요청: {}", request);
        String result = restaurantAgent.run(destination, budget, request);
        steps.add(new AgentStep("식당 Agent", request, result));
        return result;
    }
}
