package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOutputService {
    
    private final ChatClient chatClient;
    
    public ListOutputService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * List 타입으로 변환
     * 주제에 대한 목록을 List<String> 형태로 반환
     */
    public List<String> getListResult(String subject) {
        return chatClient.prompt()
                .user(u -> u.text("List five {subject}")
                        .param("subject", subject))
                .call()
                .entity(new ListOutputConverter());
    }
}
