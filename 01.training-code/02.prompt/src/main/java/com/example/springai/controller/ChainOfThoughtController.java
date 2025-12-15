package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.springai.service.ChainOfThoughtService;

import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;

@Slf4j
@Controller
@RequestMapping("/cot")
public class ChainOfThoughtController {
    
    private final ChainOfThoughtService chainOfThoughtService;

    public ChainOfThoughtController(ChainOfThoughtService chainOfThoughtService) {
        this.chainOfThoughtService = chainOfThoughtService;
    }

    /**
     * PromptTemplate을 사용한 채팅 메시지 처리
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chatPt(@RequestBody Map<String, String> request, HttpSession httpSession) {
        String userInput = request.get("message");
        String conversationId = request.get("conversationId");
        
        log.info("Received chat request with template: {} (conversationId: {})", userInput, conversationId);
        log.info("=".repeat(80));
        
        // ChainOfThoughtService를 통해 메시지 처리
        String response = chainOfThoughtService.processChat(userInput);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", response);
        responseMap.put("conversationId", conversationId != null ? conversationId : httpSession.getId());
        
        return ResponseEntity.ok(responseMap);
    }
    /**
     * 새 대화 시작
     */
    @PostMapping("/chat/new")
    @ResponseBody
    public ResponseEntity<String> newConversation() {
        String conversationId = java.util.UUID.randomUUID().toString();
        log.info("Created new conversation: {}", conversationId);
        return ResponseEntity.ok(conversationId);
    }

}
