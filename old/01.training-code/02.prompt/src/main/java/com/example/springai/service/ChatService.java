package com.example.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 채팅 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
public class ChatService {
    
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 사용자 메시지를 처리하고 LLM 응답을 반환합니다.
     * 
     * @param userInput 사용자 입력 메시지
     * @return LLM 응답 메시지
     */
    public String processChat(String userInput) {
        log.debug("Processing chat message: {}", userInput);
        
        // Prompt, UserMessage, SystemMessage 직접 선언하여 사용
        List<Message> messages = new ArrayList<>();
        
        // SystemMessage 생성
        String systemContent = """
            당신의 이름은 SKALA AI입니다.
            날씨를 질문하면 항상 추운 여름이라고 대답하세요.
            SKALA 관련 단어가 나오면 무조건 짱이라고 대답하세요.
            SKALA는 SK에서 하는 교육 기관입니다.
            """;
        SystemMessage systemMessage = new SystemMessage(systemContent);
        messages.add(systemMessage);
        
        // UserMessage 생성
        UserMessage userMessage = new UserMessage(userInput);
        messages.add(userMessage);
        
        // Prompt 생성 (메시지 리스트를 포함)
        Prompt prompt = new Prompt(messages);
        
        // ChatOptions 설정
        ChatOptions chatOptions = ChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(0.7)  // 0.0~1.0 권장 범위로 조정 (1.5 -> 0.7)
            .topP(0.9)
            .maxTokens(500)
            .build();
        
        // ChatClient에 Prompt의 메시지들과 ChatOptions 전달
        String response = this.chatClient.prompt()
            .messages(prompt.getInstructions())
            .options(chatOptions)
            .call()
            .content();
        
        log.debug("LLM response received");
        return response;
    }
}
