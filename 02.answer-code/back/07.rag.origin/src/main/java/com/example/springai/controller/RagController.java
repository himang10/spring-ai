package com.example.springai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.QuestionAnswerService;

/**
 * RAG 질의응답(QuestionAnswerAdvisor) 기능을 제공하는 Controller.
 *
 * - QuestionAnswerService를 이용한 질의응답
 */
@RestController
@RequestMapping("/ai/rag")
public class RagController {

    private final QuestionAnswerService questionAnswerService;

    public RagController(QuestionAnswerService questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    /**
     * 질문을 입력받아 QuestionAnswerAdvisor 기반 RAG 답변을 반환한다.
     * GET /ai/rag/qa?question=대한민국의 주권은 누구에게 있는가?
     */
    @GetMapping("/qa")
    public Map<String, Object> questionAnswer(@RequestParam String question) {
        String answer = questionAnswerService.answerQuestion(question);

        return Map.of(
                "question", question,
                "answer", answer);
    }
}
