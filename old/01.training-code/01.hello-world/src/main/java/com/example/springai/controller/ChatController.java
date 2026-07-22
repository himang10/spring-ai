package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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
        
        // TODO: 실습 1 - ChatClient를 사용하여 AI 응답 생성
        // 1. this.chatClient.prompt()로 프롬프트 시작
        // 2. .system()으로 시스템 메시지 설정 (예: "SKALA Cloud 기반 교육을 지원합니다")
        // 3. .user()로 사용자 입력 전달
        // 4. .call().content()로 응답 받기
        String response = null; // 여기에 AI 응답을 저장하세요
        
        // TODO: 실습 2 - 응답 Map 생성 및 반환
        // 1. HashMap 생성
        // 2. "message" 키에 AI 응답 저장
        // 3. "conversationId" 키에 conversationId 저장 (null이면 httpSession.getId() 사용)
        // 4. ResponseEntity.ok()로 반환
        
        return null; // 여기에 ResponseEntity를 반환하세요
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
