package com.example.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Step-Back 프롬프팅 기법을 활용한 채팅 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * Step-Back Prompting은 구체적인 질문에 답하기 전에 먼저 한 단계 뒤로 물러나
 * 더 일반적이고 근본적인 원리나 개념을 파악한 후 답변하는 기법입니다.
 */
@Slf4j
@Service
public class StepBackService {
    
    private final ChatClient chatClient;

    public StepBackService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Step-Back 프롬프팅을 사용하여 사용자 메시지를 처리하고 LLM 응답을 반환합니다.
     * 
     * 단일 프롬프트로 다음 과정을 LLM이 수행하도록 지시:
     * 1. 원본 질문을 더 일반적이고 추상적인 질문으로 변환
     * 2. 일반적인 질문에 대한 답변을 먼저 얻음 (배경 지식)
     * 3. 배경 지식을 바탕으로 원본 질문에 대한 구체적인 답변 생성
     * 
     * @param userInput 사용자 입력 메시지
     * @return LLM 응답 메시지
     */
    public String processChat(String userInput) {
        log.debug("Processing chat with Step-Back prompting: {}", userInput);
        
        // Step-Back 프롬프트 구성
        String systemContent = """
            내 질문에 바로 답하지 마세요. 다음 단계를 따라 답변을 생성하세요. 
            1. Step-back: 내 질문에 답하기전에 일반적으로 고려해야 할 상위 개념과 동작 구조를 먼저 설명하세요
            2. Reasoning: 위에서 설명한 결과를 기준으로 아래의 질문에서 어떤 영역부터 의심하는 것이 합리적인지 단계적으로 추론하세요
            3. Answer: 우선적으로 점검해야 할 포인트를 간단한 목록으로 정리하세요
            """;
        
        // ChatClient를 통해 LLM 호출
        String response = this.chatClient.prompt()
            .system(systemContent)
            .user(userInput)
            .call()
            .content();
        
        log.debug("Step-Back response received");
        return response;
    }
}
