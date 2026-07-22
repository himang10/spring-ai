package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PromptChatMemoryService {
    private ChatClient chatClient;
    
    public PromptChatMemoryService(
            ChatMemory chatMemory,
            ChatClient.Builder chatClientBuilder) {
        
        this.chatClient = chatClientBuilder
            .defaultAdvisors(
                // PromptChatMemoryAdvisor: 시스템 프롬프트에 텍스트로 메모리 추가
                PromptChatMemoryAdvisor.builder(chatMemory).build(),
                new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
            )
            .build();
    }
    
    public String chat(String userText, String conversationId) {
        String answer = chatClient.prompt()
            .user(userText)
            .advisors(advisorSpec -> advisorSpec.param(
                ChatMemory.CONVERSATION_ID, conversationId
            ))
            .call()
            .content();
        return answer;
    }
}
