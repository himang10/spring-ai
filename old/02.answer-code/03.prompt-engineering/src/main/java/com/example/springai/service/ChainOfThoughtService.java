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
            내 질문에 바로 답하지 마세요. 다음 단계를 따라 답변을 생성하세요.
            1. Breakdown(분해): 문제를 해결하기 위해 확인해야 할 단계를 3~6개로 나누세요. 
            2. Check(판단): 각 단계마다 무엇을 결정/계산/정리해야 하는지 그 판단에 필요한 근거가 무엇인지를 작성하세요
            3. Narrow(좁히기): 위 단계들을 바탕으로, 가능한 선택지/해석/해결 경로가 있다면 우선순위 또는 선택 기준을 제시하고 하나로 수렴해주세요
            4. Action(결론): 최종 답을 간단히 제시하고, 필요하면 요약(핵심 5~10개) 또는 다음 할 일(Next steps)로 정리하세요

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
