package com.example.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PromptTemplate을 활용한 채팅 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
public class PromptTemplateService {
    
    private final ChatClient chatClient;

    public PromptTemplateService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * PromptTemplate을 사용하여 사용자 메시지를 처리하고 LLM 응답을 반환합니다.
     * 
     * @param userInput 사용자 입력 메시지
     * @return LLM 응답 메시지
     */
    public String processChat(String userInput) {
        log.debug("Processing chat with template: {}", userInput);
        
        // Prompt, UserMessage, SystemMessage 직접 선언하여 사용
        List<Message> messages = new ArrayList<>();

        // PromptTemplate을 사용하여 SystemMessage 생성
        String educationName = "SKAKLA";
        String organizationName = "SK";
        
        /** 
         * PromptTemplate Code 추가
         */
        String systemContent = "";


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
            .temperature(0.7)
            .topP(0.9)
            .maxTokens(500)
            .build();

        // ChatClient에 Prompt의 메시지들과 ChatOptions 전달

        /** 
         * PromptTemplate Code 추가
         */
        String response = "";
        
        log.debug("LLM response received with template");
        return response;
    }
}
