package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Controller
public class ChatController {
    
    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
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
        log.info("=".repeat(80));
        
        // Prompt, UserMessage, SystemMessage, AssistantMessage 직접 선언하여 사용
        List<Message> messages = new ArrayList<>();
        
        // SystemMessage 생성
        String systemContent = "당신의 이름은 SKALA AI입니다. " +
            "날씨를 질문하면 항상 추운 여름이라고 대답하세요." +
            "SKALA 관련 단어가 나오면 무조건 짱이라고 대답하세요" + 
            "SKALA는 SK에서 하는 교육 기관입니다";
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
            .temperature(1.5)
            .topP(0.9)
            .maxTokens(500)
            .build();
        
        // ChatClient에 Prompt의 메시지들과 ChatOptions 전달
        // Prompt 객체를 생성했으므로, Prompt에 포함된 메시지들을 사용
        String response = this.chatClient.prompt()
            .messages(prompt.getInstructions())
            .options(chatOptions)
            .call()
            .content();
        
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", response);
        responseMap.put("conversationId", conversationId != null ? conversationId :  httpSession.getId());
        
        return ResponseEntity.ok(responseMap);
    }


    /**
     * 채팅 메시지 처리
     */
    @PostMapping("/api/chat/pt")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chatPt(@RequestBody Map<String, String> request, HttpSession httpSession) {
        String userInput = request.get("message");
        String conversationId = request.get("conversationId");
        
        log.info("Received chat request: {} (conversationId: {})", userInput, conversationId);
        log.info("=".repeat(80));
        
        // Prompt, UserMessage, SystemMessage, AssistantMessage 직접 선언하여 사용
        List<Message> messages = new ArrayList<>();

        // PromptTemplate을 사용하여 SystemMessage 생성
        // -------
        String educationName = "SKAKLA";
        String organizationName = "SK";
        PromptTemplate systemMessageTemplate = new PromptTemplate(
            "당신의 이름은 {educationName} AI입니다. " +
            "날씨를 질문하면 항상 추운 여름이라고 대답하세요." +
            "{educationName} 관련 단어가 나오면 무조건 짱이라고 대답하세요" + 
            "{educationName}는 {organizationName}에서 하는 교육 기관입니다"
        );
        String systemContent = systemMessageTemplate.render(Map.of(
            "educationName", educationName, 
            "organizationName", organizationName
        ));
        SystemMessage systemMessage = new SystemMessage(systemContent);
        
        messages.add(systemMessage);
        // -------

        // UserMessage 생성
        UserMessage userMessage = new UserMessage(userInput);
        messages.add(userMessage);
        
        // Prompt 생성 (메시지 리스트를 포함)
        Prompt prompt = new Prompt(messages);

        // ChatOptions 설정
        ChatOptions chatOptions = ChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(1.5)
            .topP(0.9)
            .maxTokens(500)
            .build();

        // ChatClient에 Prompt의 메시지들과 ChatOptions 전달
        // Prompt 객체를 생성했으므로, Prompt에 포함된 메시지들을 사용
        // jsonLoggingAdvisor는 ChatClientConfig에서 defaultAdvisors로 이미 등록됨
        String response = this.chatClient.prompt()
            .messages(prompt.getInstructions())
            .user(u -> u
                .text("{educationName} 을 항상 답변 마지막에 추가해줘")
                .param("educationName", educationName)
            )
            .options(chatOptions)
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
