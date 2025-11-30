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
            질문에 답변할 때 다음 단계를 따르세요:
            
            1. 일반화된 질문 도출: 주어진 구체적인 질문을 더 일반적이고 추상적인 개념이나 원리에 대한 질문으로 변환합니다.
            2. 배경 지식 설명: 일반화된 질문에 대한 배경 지식, 개념, 원리를 상세히 설명합니다.
            3. 구체적인 답변: 배경 지식을 바탕으로 원래 질문에 대해 정확하고 구체적으로 답변합니다.
            
            답변 형식:
            Step 1: 일반화된 질문
            [원본 질문의 근본이 되는 일반적인 질문]
            
            Step 2: 배경 지식
            [일반적인 개념, 원리, 맥락 설명]
            
            Step 3: 구체적인 답변
            [원본 질문에 대한 직접적이고 상세한 답변]
            
            추가 정보
            [관련된 유용한 정보나 예시]
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
