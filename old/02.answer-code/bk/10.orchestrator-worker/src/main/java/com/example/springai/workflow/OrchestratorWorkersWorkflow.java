package com.example.springai.workflow;

import com.example.springai.model.OrchestratorResponse;
import com.example.springai.model.WorkerResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Orchestrator-Workers 워크플로우: 
 * 복잡한 작업을 Orchestrator가 분석하고, Worker들이 병렬로 처리하는 패턴
 */
@Service
public class OrchestratorWorkersWorkflow {
    
    private final Orchestrator orchestrator;
    private final Worker worker;
    
    public OrchestratorWorkersWorkflow(Orchestrator orchestrator, Worker worker) {
        this.orchestrator = orchestrator;
        this.worker = worker;
    }
    
    /**
     * 전체 워크플로우 실행
     * 
     * @param taskDescription 처리할 작업 설명
     * @return 최종 결과 (분석 내용 + 각 워커의 결과)
     */
    public WorkerResponse process(String taskDescription) {
        // 1단계: Orchestrator가 작업을 분석하고 서브작업들로 분해
        OrchestratorResponse orchestratorResponse = orchestrator.analyze(taskDescription);
        
        System.out.println("=== Orchestrator 분석 결과 ===");
        System.out.println(orchestratorResponse.analysis());
        System.out.println("\n=== 처리할 하위 작업 목록 ===");
        orchestratorResponse.subTasks().forEach(task -> 
            System.out.println("- [" + task.type() + "] " + task.description())
        );
        
        // 2단계: Worker들이 서브작업들을 병렬로 처리
        System.out.println("\n=== 하위 작업 병렬 처리 중 ===");
        List<String> workerResults = worker.processSubTasksInParallel(
            orchestratorResponse.subTasks()
        );
        
        // 3단계: 결과 결합
        List<WorkerResponse.WorkerOutput> workerOutputs = IntStream.range(0, workerResults.size())
            .mapToObj(i -> new WorkerResponse.WorkerOutput(
                orchestratorResponse.subTasks().get(i).type(),
                orchestratorResponse.subTasks().get(i).description(),
                workerResults.get(i)
            ))
            .toList();
        
        // 4단계: 최종 요약 생성
        String finalSummary = generateFinalSummary(orchestratorResponse.analysis(), workerOutputs);
        
        return new WorkerResponse(
            orchestratorResponse.analysis(),
            workerOutputs,
            finalSummary
        );
    }
    
    /**
     * 모든 결과를 종합하여 최종 요약 생성
     */
    private String generateFinalSummary(String analysis, List<WorkerResponse.WorkerOutput> outputs) {
        StringBuilder summary = new StringBuilder();
        summary.append("작업 분석: ").append(analysis).append("\n\n");
        summary.append("완료된 하위 작업:\n");
        
        for (int i = 0; i < outputs.size(); i++) {
            WorkerResponse.WorkerOutput output = outputs.get(i);
            summary.append(String.format("%d. [%s] %s\n", 
                i + 1, 
                output.taskType(), 
                output.taskDescription()
            ));
        }
        
        summary.append("\n모든 하위 작업이 전문 워커에 의해 성공적으로 완료되었습니다.");
        return summary.toString();
    }
}
