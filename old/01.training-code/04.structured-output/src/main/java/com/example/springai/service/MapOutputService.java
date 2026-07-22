package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MapOutputService {
    
    private final ChatClient chatClient;
    
    public MapOutputService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * Map 타입으로 변환
     * 주제에 대한 정보를 Map<String, Object> 형태로 반환
     */
    public Map<String, Object> getMapResult(String question) {

        return null;
    }
}
