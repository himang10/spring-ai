package com.example.simpleagent.tool;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class RestaurantTool {

    private static final Logger log = LoggerFactory.getLogger(RestaurantTool.class);

    @Tool(description = "출장 지역의 식당 목록을 반환한다")
    public String getRestaurantOptions(
            @ToolParam(description = "출장 지역") String destination) throws Exception {
        log.info("[Tool 호출] getRestaurantOptions(destination={})", destination);
        // 가상 데이터를 JSON 파일에서 읽어 그대로 반환
        return new ClassPathResource("data/RestaurantAgent.json")
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
