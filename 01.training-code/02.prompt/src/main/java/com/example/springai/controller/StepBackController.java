package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.springai.service.StepBackService;

import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;

@Slf4j
@Controller
@RequestMapping("/step-back")
public class StepBackController {
    
    private final StepBackService stepBackService;

    public StepBackController(StepBackService stepBackService) {
        this.stepBackService = stepBackService;
    }

    /**
     * Step-Back 프롬프팅을 사용한 채팅 메시지 처리
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request, HttpSession httpSession) {
        String userInput = request.get("message");
        String conversationId = request.get("conversationId");
        
        log.info("Received chat request with Step-Back: {} (conversationId: {})", userInput, conversationId);
        log.info("=".repeat(80));
        
        // StepBackService를 통해 메시지 처리
        String response = stepBackService.processChat(userInput);

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
