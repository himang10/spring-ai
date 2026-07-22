package com.example.demo.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    // ChatMemory 타입으로 선언하되, MessageWindowChatMemory로 구현
    @Bean
    public ChatMemory chatMemory() {
        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();
        
        // ChatMemory 타입으로 반환하지만, MessageWindowChatMemory 인스턴스
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(repository)
            .maxMessages(10)  // 여기서만 설정 가능
            .build();
    }
}
