package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;

@Slf4j
@Controller
public class ChatController {
    
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }
    
    /**
     * 메인 채팅 페이지
     */
    @GetMapping("/")
    public String index() {
        return "chat";
    }
    
    /**
     * 채팅 메시지 처리
     */
    @PostMapping("/api/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request, HttpSession httpSession) {
        String userInput = request.get("message");
        String conversationId = request.get("conversationId");
        
        log.info("Received chat request: {} (conversationId: {})", userInput, conversationId);
        
        // AI 응답 생성
        /*  1번 Hello World 실습        
        String response = this.chatClient.prompt()
            .system("날씨나 기온 등을 질문하면 추운 가을이라고 대답하세요.")
            .user(userInput)
            .call()
            .content();
         */

        // 2번 ChatOptions 적용 실습
        ChatOptions chatOptions = ChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(1.5)
            .topP(0.9)
            .maxTokens(500)
            .build();

        String response = this.chatClient.prompt()
            .options(chatOptions)
            .user(userInput)
            .call()
            .content();

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", response);
        responseMap.put("conversationId", conversationId != null ? conversationId :  httpSession.getId());
        
        return ResponseEntity.ok(responseMap);
    }
    
    /**
     * 새 대화 시작
     */
    @PostMapping("/api/chat/new")
    @ResponseBody
    public ResponseEntity<String> newConversation() {
        String conversationId = java.util.UUID.randomUUID().toString();
        log.info("Created new conversation: {}", conversationId);
        return ResponseEntity.ok(conversationId);
    }

}
