package com.example.springai.advisor;

import com.example.springai.model.OrchestratorResponse;
import com.example.springai.model.WorkerResponse;
import com.example.springai.workflow.Orchestrator;
import com.example.springai.workflow.Worker;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Orchestrator-Workers 패턴을 Advisor로 구현
 * ChatClient의 advisor chain에 통합하여 사용할 수 있습니다.
 * 
 * Spring AI 1.0.0 기준으로 CallAdvisor, StreamAdvisor 인터페이스 사용
 * 
 * 사용 예시:
 * <pre>
 * chatClient.prompt()
 *     .advisors(new OrchestratorWorkersAdvisor(orchestrator, worker))
 *     .user("복잡한 작업 요청")
 *     .call()
 *     .content();
 * </pre>
 */
public class OrchestratorWorkersAdvisor implements CallAdvisor, StreamAdvisor {
    
    private final Orchestrator orchestrator;
    private final Worker worker;
    private final int order;
    
    public static final String ORCHESTRATOR_ANALYSIS = "orchestrator_analysis";
    public static final String WORKER_RESPONSES = "worker_responses";
    public static final String ENABLE_ORCHESTRATOR = "enable_orchestrator";
    
    public OrchestratorWorkersAdvisor(Orchestrator orchestrator, Worker worker) {
        this(orchestrator, worker, 0);
    }
    
    public OrchestratorWorkersAdvisor(Orchestrator orchestrator, Worker worker, int order) {
        this.orchestrator = orchestrator;
        this.worker = worker;
        this.order = order;
    }
    
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    /**
     * 동기 호출 시 실행되는 메서드
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        // Orchestrator 활성화 여부 확인
        boolean enableOrchestrator = (boolean) chatClientRequest.context()
            .getOrDefault(ENABLE_ORCHESTRATOR, true);
        
        if (!enableOrchestrator) {
            // Orchestrator를 사용하지 않으면 다음 advisor로 진행
            return chain.nextCall(chatClientRequest);
        }
        
        // 사용자 메시지 추출 (prompt의 마지막 UserMessage에서 텍스트 가져오기)
        String userMessage = chatClientRequest.prompt().getUserMessage().getText();
        
        // 1. Orchestrator가 작업 분석
        OrchestratorResponse orchestratorResponse = orchestrator.analyze(userMessage);
        
        // 2. Worker들이 병렬로 서브작업 처리
        List<String> workerResults = worker.processSubTasksInParallel(
            orchestratorResponse.subTasks()
        );
        
        // 3. 결과를 컨텍스트에 저장
        Map<String, Object> context = new HashMap<>(chatClientRequest.context());
        context.put(ORCHESTRATOR_ANALYSIS, orchestratorResponse.analysis());
        
        List<WorkerResponse.WorkerOutput> workerOutputs = IntStream.range(0, workerResults.size())
            .mapToObj(i -> new WorkerResponse.WorkerOutput(
                orchestratorResponse.subTasks().get(i).type(),
                orchestratorResponse.subTasks().get(i).description(),
                workerResults.get(i)
            ))
            .toList();
        
        context.put(WORKER_RESPONSES, workerOutputs);
        
        // 4. 수정된 프롬프트 생성 (worker 결과를 포함)
        String enhancedPrompt = buildEnhancedPrompt(userMessage, orchestratorResponse, workerOutputs);
        
        // 5. 수정된 요청으로 다음 advisor 체인 실행
        // prompt를 augment하여 수정
        var modifiedPrompt = chatClientRequest.prompt().augmentUserMessage(enhancedPrompt);
        
        ChatClientRequest modifiedRequest = chatClientRequest.mutate()
            .prompt(modifiedPrompt)
            .context(context)
            .build();
        
        return chain.nextCall(modifiedRequest);
    }
    
    /**
     * 스트리밍 호출 시 실행되는 메서드
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        // 스트리밍에서는 동기 방식과 동일하게 처리
        boolean enableOrchestrator = (boolean) chatClientRequest.context()
            .getOrDefault(ENABLE_ORCHESTRATOR, true);
        
        if (!enableOrchestrator) {
            return chain.nextStream(chatClientRequest);
        }
        
        String userMessage = chatClientRequest.prompt().getUserMessage().getText();
        OrchestratorResponse orchestratorResponse = orchestrator.analyze(userMessage);
        List<String> workerResults = worker.processSubTasksInParallel(
            orchestratorResponse.subTasks()
        );
        
        Map<String, Object> context = new HashMap<>(chatClientRequest.context());
        context.put(ORCHESTRATOR_ANALYSIS, orchestratorResponse.analysis());
        
        List<WorkerResponse.WorkerOutput> workerOutputs = IntStream.range(0, workerResults.size())
            .mapToObj(i -> new WorkerResponse.WorkerOutput(
                orchestratorResponse.subTasks().get(i).type(),
                orchestratorResponse.subTasks().get(i).description(),
                workerResults.get(i)
            ))
            .toList();
        
        context.put(WORKER_RESPONSES, workerOutputs);
        
        String enhancedPrompt = buildEnhancedPrompt(userMessage, orchestratorResponse, workerOutputs);
        
        var modifiedPrompt = chatClientRequest.prompt().augmentUserMessage(enhancedPrompt);
        
        ChatClientRequest modifiedRequest = chatClientRequest.mutate()
            .prompt(modifiedPrompt)
            .context(context)
            .build();
        
        return chain.nextStream(modifiedRequest);
    }
    
    /**
     * Worker 결과를 포함한 향상된 프롬프트 생성
     */
    private String buildEnhancedPrompt(String originalPrompt, 
                                      OrchestratorResponse orchestratorResponse,
                                      List<WorkerResponse.WorkerOutput> workerOutputs) {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("원본 요청: ").append(originalPrompt).append("\n\n");
        enhanced.append("작업 분석: ").append(orchestratorResponse.analysis()).append("\n\n");
        enhanced.append("전문 워커 결과:\n");
        
        for (int i = 0; i < workerOutputs.size(); i++) {
            WorkerResponse.WorkerOutput output = workerOutputs.get(i);
            enhanced.append(String.format("%d. [%s] %s\n", 
                i + 1, 
                output.taskType(),
                output.taskDescription()
            ));
            enhanced.append("결과: ").append(output.result()).append("\n\n");
        }
        
        enhanced.append("이러한 전문 결과들을 종합하여 포괄적인 최종 응답을 작성해주세요.");
        
        return enhanced.toString();
    }
}
