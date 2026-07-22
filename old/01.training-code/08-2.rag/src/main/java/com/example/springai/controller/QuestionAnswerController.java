package com.example.springai.controller;

import com.example.springai.service.QuestionAnswerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * QuestionAnswerService를 호출하여 RAG 기반의 Q&A 기능을 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/chat")
public class QuestionAnswerController {

    private final QuestionAnswerService questionAnswerService;

    public QuestionAnswerController(QuestionAnswerService questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    /**
     * RAG Q&A 엔드포인트입니다.
     *
     * @param question 사용자 질문
     * @return 질문과 답변을 포함한 Map
     */
    @PostMapping("/qa")
    public Map<String, String> qa(@RequestParam(value = "question", defaultValue = "대한민국 헌법 제1조 1항의 내용은 무엇인가요?") String question) {
        String answer = questionAnswerService.answerQuestion(question);
        return Map.of("question", question, "answer", answer);
    }
}
