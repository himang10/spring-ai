package com.example.demo.agent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.demo.advisor.CityValidationAdvisor;

@Component
public class Exam01WeatherAgent {

  private static final String SYSTEM_PROMPT = """
    당신은 날씨 정보를 제공하는 전문 에이전트입니다.
    날씨 정보가 필요하면 반드시 Tool을 사용해 조회하세요.
    추측으로 답변하지 마세요.

    ## 사용 가능한 도구
    1. getCurrentWeather: 특정 도시의 현재 날씨 조회
    2. getWeeklyForecast: 특정 도시의 주간 예보 조회
    3. getCurrentDate: 오늘 날짜 조회

    ## 규칙
    1. 사용자가 '주간', '이번 주', '예보', '5일'을 언급하면 getWeeklyForecast를 사용하세요.
    2. 사용자가 '오늘', '지금', '현재'를 언급하면 getCurrentWeather를 사용하세요.
    3. 그 외의 경우에는 질문의 의도에 맞게 적절한 Tool을 선택하거나, 예보가 불가능한 시점에 대해서는 일반적인 기후 특성을 안내하세요.
    4. 사용자가 도시를 명시하지 않고 후속 질문(예: "그럼 내일은?", "이번 주는?")을 하면, 이전 대화에서 마지막으로 언급된 도시를 기준으로 답변하세요.
  """;

  private final ChatClient chatClient;

  public Exam01WeatherAgent(ChatClient.Builder builder, ChatMemory chatMemory) {
    this.chatClient = builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultAdvisors(new CityValidationAdvisor())
        // 대화 맥락 유지를 위한 Advisor
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
  }

  // conversationId를 통해 세션별로 대화 맥락 유지
  public String execute(String conversationId, String userQuery) {
    return chatClient.prompt()
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
        .user(userQuery)
        .tools(this)
        .call()
        .content();
  }

  @Tool(description = "오늘 날짜를 조회합니다")
  public String getCurrentDate() {
    LocalDate now = LocalDate.now();
    return now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일", Locale.KOREAN));
  }

  @Tool(description = "특정 도시의 현재 날씨 정보를 조회합니다")
  public String getCurrentWeather(@ToolParam(description = "도시 이름") String city) {
    return String.format("%s의 현재 날씨는 맑고 23도입니다.", city);
  }

  @Tool(description = "특정 도시의 주간(5일) 날씨 예보를 조회합니다")
  public String getWeeklyForecast(@ToolParam(description = "도시 이름") String city) {
    return String.format("""
      [%s 5일 예보]
      - 월: 맑음 22°C
      - 화: 흐림 20°C
      - 수: 비 18°C
      - 목: 맑음 21°C
      - 금: 맑음 23°C
      """, city);
  }
}