package com.example.springai.controller;

import com.example.springai.service.CompressionQueryService2;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * PreRetrievalRAGService3를 호출하여 Compression과 Rewrite 쿼리 변환이 모두 포함된 RAG 기능을 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/chat")
public class CompressionQueryController2 {

    private final CompressionQueryService2 service;

    public CompressionQueryController2(CompressionQueryService2 service) {
        this.service = service;
    }

    /**
     * CompressionQueryTransformer와 RewriteQueryTransformer를 모두 사용한 Pre-Retrieval RAG 검색 엔드포인트입니다.
     * 대화 이력 압축 후 질문을 재작성하여 최적화된 검색을 수행합니다.
     *
     * @param question 사용자 질문
     * @param session HTTP 세션 (대화 ID로 사용)
     * @return 질문과 답변을 포함한 Map
     */
    @PostMapping("/rag/pre-retrieval-advanced")
    public Map<String, String> preRetrievalAdvancedRag(
            @RequestParam(value = "question", required = true) String question,
            HttpSession session) {
        
        String answer = service.answer(question, session.getId());
        return Map.of("question", question, "answer", answer);
    }
}
