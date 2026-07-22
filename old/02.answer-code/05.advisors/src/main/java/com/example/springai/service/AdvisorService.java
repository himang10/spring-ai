package com.example.springai.service;

import com.example.springai.advisor.JsonLoggingAdvisor;
import com.example.springai.advisor.SimpleLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Advisor 기능을 테스트하는 서비스 클래스.
 * SimpleLoggingAdvisor와 JsonLoggingAdvisor를 동적으로 적용하여
 * LLM 호출 시 로깅 방식을 선택할 수 있습니다.
 * 
 * @author SKALA Team
 * @since 1.0.0
 */
@Service
public class AdvisorService {

    private final ChatClient.Builder chatClientBuilder;
    private final SimpleLoggingAdvisor simpleLoggingAdvisor;
    private final JsonLoggingAdvisor jsonLoggingAdvisor;

    public AdvisorService(ChatClient.Builder chatClientBuilder, 
                         SimpleLoggingAdvisor simpleLoggingAdvisor,
                         JsonLoggingAdvisor jsonLoggingAdvisor) {
        this.chatClientBuilder = chatClientBuilder;
        this.simpleLoggingAdvisor = simpleLoggingAdvisor;
        this.jsonLoggingAdvisor = jsonLoggingAdvisor;
    }

    /**
     * SimpleLoggingAdvisor를 사용하여 간단한 텍스트 형식으로 로깅하며 LLM을 호출합니다.
     * 
     * @param userMessage 사용자 메시지
     * @return AI 응답
     */
    public String callWithSimpleLogging(String userMessage) {
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(simpleLoggingAdvisor)
                .build();
        
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * JsonLoggingAdvisor를 사용하여 JSON 형식으로 로깅하며 LLM을 호출합니다.
     * 
     * @param userMessage 사용자 메시지
     * @return AI 응답
     */
    public String callWithJsonLogging(String userMessage) {
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(jsonLoggingAdvisor)
                .build();
        
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
