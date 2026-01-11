package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MultiMessageService {
    private ChatClient chatClient;
    private ChatMemory chatMemory;
    
    public MultiMessageService(
            ChatMemory chatMemory, 
            ChatClient.Builder chatClientBuilder) {
        this.chatMemory = chatMemory;
        this.chatClient = chatClientBuilder.build();
    }
    
    public String chat(String question, String conversationId) {
        // 시스템 메시지 추가 (대화 처음 시작할 경우)
        if (chatMemory.get(conversationId).isEmpty()) {
            SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                    당신은 AI 비서입니다.
                    제공되는 지난 대화 내용을 보고 우선적으로 답변해주세요.
                    """)
                .build();
            chatMemory.add(conversationId, systemMessage);
        }
        
        // 이전 대화 내역 출력
        log.info("대화 내역: {}", chatMemory.get(conversationId));
        
        // 대화 메시지 저장
        UserMessage userMessage = UserMessage.builder()
            .text(question)
            .build();
        chatMemory.add(conversationId, userMessage);
        
        // LLM에게 요청하고 응답받기
        String response = chatClient.prompt()
            .messages(chatMemory.get(conversationId))
            .user(question)
            .call()
            .content();
        
        // 응답을 메모리에 추가
        AssistantMessage assistantMessage = new AssistantMessage(response);
        chatMemory.add(conversationId, assistantMessage);
        
        // LLM의 텍스트 답변 추출
        return response;
    }
}
