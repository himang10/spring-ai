package com.example.springai.controller;

import com.example.springai.advisor.OrchestratorWorkersAdvisor;
import com.example.springai.workflow.Orchestrator;
import com.example.springai.workflow.Worker;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Advisor 패턴을 사용한 Orchestrator-Workers 구현
 * 
 * 이 방식은 Spring AI의 Advisor chain에 통합되어
 * 다른 Advisor들(RAG, Memory 등)과 함께 사용할 수 있습니다.
 */
@RestController
@RequestMapping("/api/advisor")
public class AdvisorBasedController {
    
    private final ChatClient chatClient;
    
    public AdvisorBasedController(ChatClient.Builder chatClientBuilder,
                                  Orchestrator orchestrator,
                                  Worker worker) {
        // ChatClient에 OrchestratorWorkersAdvisor를 기본으로 설정
        this.chatClient = chatClientBuilder
            .defaultAdvisors(new OrchestratorWorkersAdvisor(orchestrator, worker))
            .build();
    }
    
    /**
     * Advisor 패턴으로 Orchestrator-Workers 실행
     * 
     * Orchestrator가 자동으로 작업을 분석하고
     * Worker들이 처리한 결과를 종합하여 최종 응답을 생성합니다.
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processWithAdvisor(@RequestBody TaskRequest request) {
        // ChatClient의 advisor chain이 자동으로 Orchestrator-Workers를 실행
        String response = chatClient.prompt()
            .user(request.task())
            .call()
            .content();
        
        return ResponseEntity.ok(Map.of(
            "response", response,
            "method", "advisor-based"
        ));
    }
    
    /**
     * Advisor를 선택적으로 비활성화
     */
    @PostMapping("/process-direct")
    public ResponseEntity<Map<String, Object>> processDirect(@RequestBody TaskRequest request) {
        // Orchestrator를 비활성화하고 직접 처리
        String response = chatClient.prompt()
            .advisors(a -> a.param(OrchestratorWorkersAdvisor.ENABLE_ORCHESTRATOR, false))
            .user(request.task())
            .call()
            .content();
        
        return ResponseEntity.ok(Map.of(
            "response", response,
            "method", "direct"
        ));
    }
    
    /**
     * 예제 엔드포인트
     */
    @GetMapping("/example")
    public ResponseEntity<Map<String, Object>> example() {
        // String task = "Create a comprehensive REST API design for a user management system";
        String task = "사용자 관리 시스템을 위한 포괄적인 REST API 설계를 작성하세요.";
        
        String response = chatClient.prompt()
            .user(task)
            .call()
            .content();
        
        return ResponseEntity.ok(Map.of(
            "task", task,
            "response", response,
            "method", "advisor-based"
        ));
    }
    
    public record TaskRequest(String task) {}
}
