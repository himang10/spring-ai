package com.example.springai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String conversationId;
    private long timestamp;
    
    public ChatResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
