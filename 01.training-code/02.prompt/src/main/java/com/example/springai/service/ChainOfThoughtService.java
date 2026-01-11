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
            당신은 논리적으로 문제를 해결하는 분석가입니다.
            문제를 해결할 때 다음 단계를 따르세요:
            1. 먼저 문제를 명확히 이해하고 요약합니다.
            2. 문제를 해결하기 위한 단계별 사고 과정을 설명합니다.
            3. 각 단계의 추론 과정을 상세히 보여줍니다.
            4. 최종 답변을 도출합니다.
            
            답변 형식:
            1. 문제 이해
            [문제 요약]
            
            2. 사고 과정
            [단계별 추론]

            3. 추론 과정
            [상세 설명]
            
            4. 최종 답변
            [결론]
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
