package com.example.springai.config;

import com.example.springai.advisor.JsonLoggingAdvisor;
import com.example.springai.advisor.SimpleLoggingAdvisor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public SimpleLoggingAdvisor simpleLoggingAdvisor() {
        return new SimpleLoggingAdvisor();
    }

    @Bean
    public JsonLoggingAdvisor jsonLoggingAdvisor(ObjectMapper objectMapper) {
        return new JsonLoggingAdvisor(objectMapper, 0);
    }
}
