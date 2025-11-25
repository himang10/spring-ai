package com.example.springai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatClient chatClient;
    
    // 대화 이력을 메모리에 저장 (conversationId -> messages)
    private final Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();
    
    /**
     * 메시지를 처리하고 AI 응답을 반환합니다.
     */
    public String chat(String userMessage, String conversationId) {
        log.info("Processing message for conversation: {}", conversationId);
        
        // 대화 이력 가져오기 또는 생성
        List<Message> messages = conversationHistory.computeIfAbsent(
            conversationId, 
            k -> new ArrayList<>()
        );
        
        // 사용자 메시지 추가
        messages.add(new UserMessage(userMessage));
        
        try {
            // ChatClient를 사용하여 응답 생성
            String response = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();
            
            // AI 응답을 대화 이력에 추가
            messages.add(new AssistantMessage(response));
            
            log.info("Response generated successfully");
            return response;
            
        } catch (Exception e) {
            log.error("Error generating response", e);
            throw new RuntimeException("AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 특정 대화의 이력을 초기화합니다.
     */
    public void clearConversation(String conversationId) {
        conversationHistory.remove(conversationId);
        log.info("Conversation history cleared for: {}", conversationId);
    }
    
    /**
     * 모든 대화 이력을 초기화합니다.
     */
    public void clearAllConversations() {
        conversationHistory.clear();
        log.info("All conversation histories cleared");
    }
}
