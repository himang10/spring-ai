package com.example.springai.controller;

import com.example.springai.dto.ChatRequest;
import com.example.springai.dto.ChatResponse;
import com.example.springai.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
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
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.getMessage());
        
        try {
            // conversationId가 없으면 새로 생성
            String conversationId = request.getConversationId();
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = UUID.randomUUID().toString();
            }
            
            // AI 응답 생성
            String response = chatService.chat(request.getMessage(), conversationId);
            
            return ResponseEntity.ok(new ChatResponse(
                response, 
                conversationId, 
                System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 대화 이력 초기화
     */
    @DeleteMapping("/api/chat/{conversationId}")
    @ResponseBody
    public ResponseEntity<Void> clearConversation(@PathVariable String conversationId) {
        log.info("Clearing conversation: {}", conversationId);
        chatService.clearConversation(conversationId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 새 대화 시작
     */
    @PostMapping("/api/chat/new")
    @ResponseBody
    public ResponseEntity<String> newConversation() {
        String newConversationId = UUID.randomUUID().toString();
        log.info("New conversation created: {}", newConversationId);
        return ResponseEntity.ok(newConversationId);
    }
}
