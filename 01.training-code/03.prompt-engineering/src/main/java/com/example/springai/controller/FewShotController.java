package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.springai.service.FewShotService;

import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;

/**
 * Few-Shot Learning 기반 감성 분석 API를 제공하는 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/few-shot")
public class FewShotController {
    
    private final FewShotService fewShotService;

    public FewShotController(FewShotService fewShotService) {
        this.fewShotService = fewShotService;
    }
    
    /**
     * Few-Shot 감성 분석 메시지 처리
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request, HttpSession httpSession) {
        String userInput = request.get("message");
        String conversationId = request.get("conversationId");
        
        log.info("Received Few-Shot sentiment analysis request: {} (conversationId: {})", userInput, conversationId);
        log.info("=".repeat(80));
        
        // FewShotService를 통해 감성 분석 처리
        String response = fewShotService.processChat(userInput);
        
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", response);
        responseMap.put("conversationId", conversationId != null ? conversationId : httpSession.getId());
        
        return ResponseEntity.ok(responseMap);
    }
}
