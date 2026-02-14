package com.example.springai.controller;

import com.example.springai.service.RetrievalRAGService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * RetrievalRagService를 호출하여 기본 RAG 기능을 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/chat")
public class RetrievalRAGController {

    private final RetrievalRAGService service;

    public RetrievalRAGController(RetrievalRAGService service) {
        this.service = service;
    }

    /**
     * 기본 RAG 검색 엔드포인트입니다.
     *
     * @param question 사용자 질문
     * @return 질문과 답변을 포함한 Map
     */
    @PostMapping("/rag/retrieval")
    public Map<String, String> retrievalRag(@RequestParam(value = "question", required=true) String question) {
        String answer = service.answer(question);
        return Map.of("question", question, "answer", answer);
    }
}
