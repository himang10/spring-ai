package com.example.springai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.RewriteQueryService;

import java.util.Map;

/**
 * PreRetrievalRAGService를 호출하여 쿼리 변환이 포함된 RAG 기능을 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/chat")
public class RewriteQueryController {

    private final RewriteQueryService service;

    public RewriteQueryController(RewriteQueryService service) {
        this.service = service;
    }

    /**
     * Pre-Retrieval RAG 검색 엔드포인트입니다.
     *
     * @param question 사용자 질문
     * @return 질문과 답변을 포함한 Map
     */
    @PostMapping("/rag/pre-retrieval")
    public Map<String, String> preRetrievalRag(@RequestParam(value = "question", required = true) String question) {
        String answer = service.answer(question);
        return Map.of("question", question, "answer", answer);
    }
}
