package com.example.springai.controller;

import com.example.springai.service.PostRetrievalRAGService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * PostRetrievalRAGService를 호출하여 컨텍스트 증강이 포함된 RAG 기능을 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/chat")
public class PostRetrievalRAGController {

    private final PostRetrievalRAGService service;

    public PostRetrievalRAGController(PostRetrievalRAGService service) {
        this.service = service;
    }

    /**
     * Post-Retrieval RAG 검색 엔드포인트입니다.
     *
     * @param question 사용자 질문
     * @return 질문과 답변을 포함한 Map
     */
    @PostMapping("/rag/post-retrieval")
    public Map<String, String> postRetrievalRag(@RequestParam(value = "question", required = true) String question) {
        String answer = service.answer(question);
        return Map.of("question", question, "answer", answer);
    }
}
