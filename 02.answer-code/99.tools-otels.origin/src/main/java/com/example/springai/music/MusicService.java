package com.example.springai.music;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 음악 관련 AI 채팅 서비스를 제공합니다.
 */
@Service
@Slf4j
public class MusicService {

    private final ChatClient chatClient;

    @Autowired
    private MusicTools musicTools;

    public MusicService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 음악 관련 질문에 대해 AI와 대화합니다.
     *
     * @param message 사용자 메시지
     * @return AI 응답
     */
    public String chat(String message) {
        log.info("음악 서비스 질문: {}", message);
        
        String answer = this.chatClient.prompt()
                .user(message)
                .tools(musicTools)
                .call()
                .content();
        
        log.info("음악 서비스 응답: {}", answer);
        return answer;
    }
}
