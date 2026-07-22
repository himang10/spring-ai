package com.example.springai.config;

import com.example.springai.advisor.JsonLoggingAdvisor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public JsonLoggingAdvisor jsonLoggingAdvisor(ObjectMapper objectMapper) {
        return new JsonLoggingAdvisor(objectMapper, 0);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, JsonLoggingAdvisor jsonLoggingAdvisor) {
        return builder
                .defaultSystem("사용자의 질문에 대해 정확한 의미를 파악하고 답변해주세요." +
                              "질문이 모호한 경우 또는 여러 선택지가 필요한 경우에는 추가 질문을 통해 명확히 해주세요.")
                .defaultAdvisors(jsonLoggingAdvisor)
                .build();
    }
}
