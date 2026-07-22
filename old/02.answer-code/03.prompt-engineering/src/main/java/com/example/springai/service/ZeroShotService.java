package com.example.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.stereotype.Service;

import com.example.springai.advisor.JsonLoggingAdvisor;


/**
 * Few-Shot Learning 기법을 활용한 감성 분석 서비스 클래스
 * 
 * Few-Shot Learning은 적은 수의 예시를 제공하여 LLM이 패턴을 학습하고
 * 동일한 형식으로 새로운 입력을 처리하도록 하는 기법입니다.
 */
@Slf4j
@Service
public class ZeroShotService {
    
    private final ChatClient chatClient;

    public ZeroShotService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Few-Shot 예시를 활용하여 고객 리뷰의 감성을 분석합니다.
     * 
     * @param userInput 사용자 입력 메시지 (고객 리뷰)
     * @return 감성 분석 결과 (긍정/부정/중립 및 이유)
     */
    public String processChat(String userInput) {
        log.debug("Processing sentiment analysis with Zero-Shot: {}", userInput);
        
        // ChatClient를 통해 LLM 호출
        /**
        String response = this.chatClient.prompt()
            .user(userInput)
            .call()
            .content();
        **/
        ChatClientRequestSpec requestSpec = this.chatClient.prompt(); 
        requestSpec = requestSpec.user(userInput);
        requestSpec = requestSpec.advisors(new JsonLoggingAdvisor());
        CallResponseSpec callResponseSpec = requestSpec.call();
        //ChatResponse chatResponse = callResponseSpec.chatResponse();
        // String response = chatResponse.getResults().get(0).getOutput().getText();
        String response = callResponseSpec.content();

        
        log.debug("Zero-Shot sentiment analysis completed");
        return response;
    }
}
