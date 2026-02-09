package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.springai.service.ReActService;

import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;

/**
 * ReAct (Reasoning and Acting) 기반 채팅 API를 제공하는 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/react")
public class ReActController {
    
    private final ReActService reActService;

    public ReActController(ReActService reActService) {
        this.reActService = reActService;
    }
    
    /**
     * ReAct 프롬프팅 메시지 처리
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request, HttpSession httpSession) {
        String userInput = request.get("message");
        String conversationId = request.get("conversationId");
        
        log.info("Received ReAct request: {} (conversationId: {})", userInput, conversationId);
        log.info("=".repeat(80));
        
        // ReActService를 통해 메시지 처리
        String response = reActService.processChat(userInput);
        
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", response);
        responseMap.put("conversationId", conversationId != null ? conversationId : httpSession.getId());
        
        return ResponseEntity.ok(responseMap);
    }
}
