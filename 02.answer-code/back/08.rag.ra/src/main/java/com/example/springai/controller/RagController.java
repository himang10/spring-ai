package com.example.springai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.qarag.QuestionAnswerService;
import com.example.springai.rarag.CompressionQueryService;
import com.example.springai.rarag.RewriteQueryService;
import jakarta.servlet.http.HttpSession;

/**
 * RAG 질의응답(QuestionAnswerAdvisor) 기능을 제공하는 Controller.
 *
 * - QuestionAnswerService를 이용한 질의응답
 * - RewriteQueryService를 이용한 질의응답(질문 재작성 기반)
 * - CompressionQueryService를 이용한 질의응답(대화 이력 압축 기반)
 */
@RestController
@RequestMapping("/ai/rag")
public class RagController {

    private final QuestionAnswerService questionAnswerService;
    private final RewriteQueryService rewriteQueryService;
    private final CompressionQueryService compressionQueryService;

    public RagController(QuestionAnswerService questionAnswerService, RewriteQueryService rewriteQueryService,
            CompressionQueryService compressionQueryService) {
        this.questionAnswerService = questionAnswerService;
        this.rewriteQueryService = rewriteQueryService;
        this.compressionQueryService = compressionQueryService;
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

    /**
     * 질문을 입력받아 쿼리 재작성(RewriteQueryTransformer) 기반 RAG 답변을 반환한다.
     * GET /ai/rag/rewrite?question=대한민국의 주권은 누구에게 있는가?
     */
    @GetMapping("/rewrite")
    public Map<String, Object> rewriteQuery(@RequestParam String question) {
        String answer = rewriteQueryService.answer(question);

        return Map.of(
                "question", question,
                "answer", answer);
    }

    /**
     * 질문을 입력받아 대화 이력 압축(CompressionQueryTransformer) 기반 RAG 답변을 반환한다.
     * 동일한 conversationId로 여러 번 호출하면 이전 대화 맥락을 반영해 후속 질문을 압축한다.
     * GET /ai/rag/compression?question=그럼 그건 언제야?&conversationId=user-1
     */
    @GetMapping("/compression")
    public Map<String, Object> compressionQuery(@RequestParam String question,
            HttpSession session) {
        String answer = compressionQueryService.answer(question, session.getId());

        return Map.of(
                "question", question,
                "conversationId", session.getId(),
                "answer", answer);
    }
}
