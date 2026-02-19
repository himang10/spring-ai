package com.example.springai.workflow;

import com.example.springai.model.OrchestratorResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Orchestrator: 복잡한 작업을 분석하고 서브작업들로 분해하는 역할
 */
@Component
public class Orchestrator {
    
    private final ChatClient chatClient;
    
    public Orchestrator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    /**
     * 주어진 작업을 분석하고 서브작업들로 분해
     */
    public OrchestratorResponse analyze(String taskDescription) {
        String prompt = """
            당신은 작업을 분석하고 하위 작업으로 분해하는 오케스트레이터 에이전트입니다.
            
            다음 작업이 주어졌습니다: %s
            
            작업을 분석하여:
            1. 수행해야 할 내용에 대한 간략한 분석을 제공하세요
            2. 서로 다른 전문가가 처리할 수 있는 구체적인 하위 작업으로 분해하세요
            3. 각 하위 작업에 대해 다음을 지정하세요:
               - 수행해야 할 작업에 대한 명확한 설명
               - 필요한 전문가 유형 (예: technical, documentation, testing, design)
            
            다음 구조의 JSON 형식으로 응답하세요:
            {
              analysis: 작업에 대한 간략한 분석,
              subTasks: [
                {
                  description: 하위 작업에 대한 상세 설명,
                  type: 전문가 유형
                }
              ]
            }
            """.formatted(taskDescription);
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(OrchestratorResponse.class);
    }
}
