package com.example.springai.model;

import java.util.List;

/**
 * Worker들의 작업 결과를 포함하는 최종 응답
 */
public record WorkerResponse(
    String analysis,
    List<WorkerOutput> workerResponses,
    String finalSummary
) {
    public record WorkerOutput(
        String taskType,
        String taskDescription,
        String result
    ) {}
}
