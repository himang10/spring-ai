package com.example.springai.model;

import java.util.List;

/**
 * Orchestrator가 작업을 분석한 결과
 */
public record OrchestratorResponse(
    String analysis,
    List<SubTask> subTasks
) {
    public record SubTask(
        String description,
        String type  // technical, documentation, testing, etc.
    ) {}
}
