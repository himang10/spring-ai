package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageChatMemoryService {
    private ChatClient chatClient;
    private MessageWindowChatMemory messageWindowChatMemory;
    
    public MessageChatMemoryService(ChatClient.Builder chatClientBuilder) {
        // MessageWindowChatMemory: 최근 N개의 메시지만 유지하는 윈도우 방식 메모리
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
            .maxMessages(10)  // 최근 10개의 메시지만 저장
            .build();
        
        this.chatClient = chatClientBuilder
            .defaultAdvisors(
                // MessageWindowChatMemory를 사용한 MessageChatMemoryAdvisor
                MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build(),
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
