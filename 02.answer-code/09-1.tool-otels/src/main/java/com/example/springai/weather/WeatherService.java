package com.example.springai.weather;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 관련 AI 채팅 서비스를 제공합니다.
 */
@Service
@Slf4j
public class WeatherService {

    private final ChatClient chatClient;

    @Autowired
    private WeatherTools weatherTools;

    public WeatherService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 날씨 관련 질문에 대해 AI와 대화합니다.
     *
     * @param question 사용자 질문
     * @return AI 응답
     */
    public String chat(String question) {
        log.info("날씨 서비스 질문: {}", question);
        
        String answer = this.chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1))
                .system("""
                        날씨 정보를 조회할 때는 다음 형식으로 결과를 보기 좋게 정리해서 사용자에게 제공하세요:
                        
                        [도시명 날씨 정보]
                        - 온도: X°C
                        - 체감 온도: X°C
                        - 날씨: 날씨 상태
                        - 습도: X%
                        - 풍속: X m/s
                        - 구름: X%
                        
                        위 항목들을 리스트 형태로 깔끔하게 표현해주세요.
                        """)
                .user(question)
                .tools(weatherTools)
                .call()
                .content();
        
        log.info("날씨 서비스 응답: {}", answer);
        return answer;
    }
}
