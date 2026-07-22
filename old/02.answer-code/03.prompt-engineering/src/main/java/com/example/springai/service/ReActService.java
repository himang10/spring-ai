package com.example.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * ReAct (Reasoning and Acting) 프롬프팅 기법을 활용한 서비스 클래스
 * 
 * ReAct는 사고(Reasoning)와 행동(Acting)을 결합하여 
 * 도구를 사용하면서 단계별로 문제를 해결하는 기법입니다.
 * 
 * ToolService의 @Tool 어노테이션이 적용된 메서드들을 .tools()로 전달합니다.
 */
@Slf4j
@Service
public class ReActService {
    
    private final ChatClient chatClient;
    private final ToolService toolService;

    public ReActService(ChatClient chatClient, ToolService toolService) {
        this.chatClient = chatClient;
        this.toolService = toolService;
    }

    /**
     * ReAct 프롬프팅을 사용하여 사용자 메시지를 처리합니다.
     * ToolService의 @Tool 어노테이션이 적용된 메서드들을 사용합니다.
     * 
     * @param userInput 사용자 입력 메시지
     * @return LLM 응답 메시지
     */
    public String processChat(String userInput) {
        log.debug("Processing chat with ReAct using @Tool methods: {}", userInput);
        
        // ReAct 시스템 프롬프트
/*         String systemContent = """
            당신은 ReAct (Reasoning and Acting) 방식으로 작동하는 AI 어시스턴트입니다.
            
            사용 가능한 도구들:
            - getCurrentTime: 현재 시간 정보를 가져옵니다.
            - getWeatherInfo: 현재 날씨 정보를 가져옵니다.
            - getLocationInfo: 현재 위치 정보를 가져옵니다.
            - getStatusInfo: 현재 상황 정보를 가져옵니다.
            - getAllInfo: 모든 정보를 한번에 가져옵니다.
            
            문제 해결 과정을 다음과 같이 표시하세요:
            
            1. Thought (사고): 질문을 분석하고 어떤 정보가 필요한지 판단합니다.
            2. Action (행동): 필요한 도구를 선택하고 호출합니다.
            3. Observation (관찰): 도구 실행 결과를 확인합니다.
            4. Answer (답변): 수집한 정보를 바탕으로 최종 답변을 제공합니다.
            
            답변 형식:
            Thought: [질문 분석 및 필요한 정보 판단]
            Action: [사용할 도구 선택 및 이유]
            Observation: [도구 실행 결과]
            Answer: [최종 답변]
            
            사용자가 시간, 날씨, 위치, 상황에 대해 물어보면 반드시 해당하는 도구를 호출하세요.
            여러 정보가 필요하면 getAllInfo를 사용하세요.
            """; */
        String systemContent = """
            당신은  SKALA AI Asistant입니다.
            """;
        
        // @Tool 어노테이션이 적용된 ToolService 인스턴스를 .tools()로 전달
        String response = this.chatClient.prompt()
            .system(systemContent)
            .user(userInput)
            .tools(toolService)
            .call()
            .content();
        
        log.debug("ReAct response with @Tool methods received");
        return response;
    }
}
