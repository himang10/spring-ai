package com.example.springai.filesystem;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 시스템 관련 AI 채팅 서비스를 제공합니다.
 */
@Service
@Slf4j
public class FileSystemService {

    private final ChatClient chatClient;

    @Autowired
    private FileSystemTools fileSystemTools;

    public FileSystemService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .build();
    }

    /**
     * 파일 시스템 관련 질문에 대해 AI와 대화합니다.
     *
     * @param question 사용자 질문
     * @return AI 응답
     */
    public String chat(String question) {
        log.info("파일시스템 서비스 질문: {}", question);
        
        String answer = this.chatClient.prompt()
                .system("""
                        날씨 정보를 조회할 때는 다음 형식으로 결과를 보기 좋게 정리해서 사용자에게 제공하세요:
                        파일 저장후 저장 결과는 다음과 같이 보기 좋게 정리해서 사용자에게 제공하세요:
                        
                        [파일 저장 결과]
                        - 파일명: X
                        - 경로: X
                        - 내용: X
                        
                        위 항목들을 리스트 형태로 깔끔하게 표현해주세요.
                        """)
                .user(question)
                .tools(fileSystemTools)
                .advisors(new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1))
                .call()
                .content();
        
        log.info("파일시스템 서비스 응답: {}", answer);
        return answer;
    }
}
