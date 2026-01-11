package com.example.springai.controller;

import com.example.springai.model.WorkerResponse;
import com.example.springai.workflow.OrchestratorWorkersWorkflow;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Orchestrator-Workers 패턴을 테스트하기 위한 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/orchestrator")
public class OrchestratorController {
    
    private final OrchestratorWorkersWorkflow workflow;
    
    public OrchestratorController(OrchestratorWorkersWorkflow workflow) {
        this.workflow = workflow;
    }
    
    /**
     * 복잡한 작업을 처리하는 엔드포인트
     * 
     * 예제 요청:
     * POST /api/orchestrator/process
     * {
     *   "task": "Create a REST API for user management with CRUD operations"
     * }
     */
    @PostMapping("/process")
    public ResponseEntity<WorkerResponse> processTask(@RequestBody TaskRequest request) {
        WorkerResponse response = workflow.process(request.task());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 간단한 GET 엔드포인트 (테스트용)
     */
    @GetMapping("/example")
    public ResponseEntity<WorkerResponse> getExample() {
        String exampleTask = """
            Generate both technical and user-friendly documentation for a REST API endpoint 
            that handles user authentication including login, logout, and token refresh.
            """;
        
        WorkerResponse response = workflow.process(exampleTask);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 요청 DTO
     */
    public record TaskRequest(String task) {}
}
