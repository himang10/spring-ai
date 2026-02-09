package com.example.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Chain of Thought를 활용한 채팅 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
public class ChainOfThoughtService {
    
    private final ChatClient chatClient;

    public ChainOfThoughtService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Chain of Thought를 사용하여 사용자 메시지를 처리하고 LLM 응답을 반환합니다.
     * 
     * @param userInput 사용자 입력 메시지
     * @return LLM 응답 메시지
     */
    public String processChat(String userInput) {
        log.debug("Processing chat with Chain of Thought: {}", userInput);
        
        // Chain of Thought 프롬프트 구성
        String systemContent = """
            여기에 Chain of Thought 지침을 작성하세요.
            """;
        
        // ChatClient를 통해 LLM 호출
        String response = this.chatClient.prompt()
            .system(systemContent)
            .user(userInput)
            .call()
            .content();
        
        log.debug("Chain of Thought response received");
        return response;
    }
}
