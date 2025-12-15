package com.example.springai.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.QueryTransformerService2;

import java.util.Map;

/**
 * PreRetrievalRAGService2를 호출하여 CompressionQueryTransformer가 포함된 RAG 기능을 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/chat")
public class QueryTransformerController2 {

    private final QueryTransformerService2 service;

    public QueryTransformerController2(QueryTransformerService2 service) {
        this.service = service;
    }

    /**
     * CompressionQueryTransformer를 사용한 Pre-Retrieval RAG 검색 엔드포인트입니다.
     * 대화 이력을 압축하여 더 정확한 검색을 수행합니다.
     *
     * @param question 사용자 질문
     * @param session HTTP 세션 (대화 ID로 사용)
     * @return 질문과 답변을 포함한 Map
     */
    @PostMapping("/rag/pre-retrieval-compression")
    public Map<String, String> preRetrievalCompressionRag(
            @RequestParam(value = "question", required = true) String question,
            HttpSession session) {
        
        String answer = service.answer(question, session.getId());
        return Map.of("question", question, "answer", answer);
    }
}
