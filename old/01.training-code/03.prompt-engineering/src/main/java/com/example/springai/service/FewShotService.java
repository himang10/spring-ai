package com.example.springai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Few-Shot Learning 기법을 활용한 감성 분석 서비스 클래스
 * 
 * Few-Shot Learning은 적은 수의 예시를 제공하여 LLM이 패턴을 학습하고
 * 동일한 형식으로 새로운 입력을 처리하도록 하는 기법입니다.
 */
@Slf4j
@Service
public class FewShotService {
    
    private final ChatClient chatClient;

    public FewShotService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Few-Shot 예시를 활용하여 고객 리뷰의 감성을 분석합니다.
     * 
     * @param userInput 사용자 입력 메시지 (고객 리뷰)
     * @return 감성 분석 결과 (긍정/부정/중립 및 이유)
     */
    public String processChat(String userInput) {
        log.debug("Processing sentiment analysis with Few-Shot: {}", userInput);
        
        // Few-Shot 프롬프트 템플릿
        String fewShotTemplate = """
            당신은 고객 리뷰 감성 분석기입니다.
            아래 예시를 참고하여, 입력된 리뷰의 감정을 '긍정', '부정', '중립' 중 하나로 분류하고 그 이유를 짧게 요약하세요.
            
            [예시 1]
            입력: "배송이 너무 느려서 화가 났지만, 제품 품질은 나쁘지 않네요."
            출력: 중립 (배송은 불만족스럽지만 품질은 만족스러움)
            
            [예시 2]
            입력: "이 가격에 이런 성능이라니 믿을 수가 없어요! 강력 추천합니다."
            출력: 긍정 (가격 대비 성능이 매우 뛰어나고 추천 의사가 있음)
            
            [예시 3]
            입력: "박스가 다 찌그러져서 왔고 내용물도 부서져 있었습니다. 최악입니다."
            출력: 부정 (포장 상태 불량 및 제품 파손 발생)
            
            [실제 작업]
            입력: {message}
            출력: 
            """;
        
        // PromptTemplate을 사용하여 메시지 치환
        PromptTemplate promptTemplate = new PromptTemplate(fewShotTemplate);
        String prompt = promptTemplate.render(Map.of("message", userInput));
        
        // ChatClient를 통해 LLM 호출
        String response = this.chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        log.debug("Few-Shot sentiment analysis completed");
        return response;
    }
}
