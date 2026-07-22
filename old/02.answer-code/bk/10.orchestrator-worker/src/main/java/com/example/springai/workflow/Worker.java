package com.example.springai.workflow;

import com.example.springai.model.OrchestratorResponse.SubTask;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Worker: 특정 전문 분야의 서브작업을 처리하는 역할
 */
@Component
public class Worker {
    
    private final ChatClient chatClient;
    private final Executor executor;
    
    // 각 전문 분야별 프롬프트 템플릿
    private static final String TECHNICAL_WORKER_PROMPT = """
        당신은 기술 전문가입니다.
        당신의 작업: {task}
        
        다음을 포함한 상세한 기술 솔루션을 제공하세요:
        - 구현 방법
        - 기술적 고려사항
        - 해당하는 경우 코드 예제
        """;
    
    private static final String DOCUMENTATION_WORKER_PROMPT = """
        당신은 문서화 전문가입니다.
        당신의 작업: {task}
        
        다음을 포함한 포괄적인 문서를 제공하세요:
        - 명확한 설명
        - 사용 예제
        - 모범 사례
        """;
    
    private static final String TESTING_WORKER_PROMPT = """
        당신은 테스트 전문가입니다.
        당신의 작업: {task}
        
        다음을 포함한 테스트 권장사항을 제공하세요:
        - 테스트 시나리오
        - 테스트 케이스
        - 예상 결과
        """;
    
    private static final String DEFAULT_WORKER_PROMPT = """
        당신은 전문 어시스턴트입니다.
        당신의 작업: {task}
        
        이 작업을 완료하기 위한 상세하고 철저한 응답을 제공하세요.
        """;
    
    public Worker(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.executor = Executors.newVirtualThreadPerTaskExecutor(); // Java 21 Virtual Threads
    }
    
    /**
     * 단일 서브작업을 처리
     */
    public String processSubTask(SubTask subTask) {
        String prompt = getPromptForType(subTask.type());
        
        return chatClient.prompt()
            .user(u -> u.text(prompt)
                .param("task", subTask.description()))
            .call()
            .content();
    }
    
    /**
     * 여러 서브작업을 병렬로 처리
     */
    public List<String> processSubTasksInParallel(List<SubTask> subTasks) {
        List<CompletableFuture<String>> futures = subTasks.stream()
            .map(subTask -> CompletableFuture.supplyAsync(
                () -> processSubTask(subTask),
                executor
            ))
            .toList();
        
        return futures.stream()
            .map(CompletableFuture::join)
            .toList();
    }
    
    /**
     * 작업 타입에 맞는 프롬프트 선택
     */
    private String getPromptForType(String type) {
        return switch (type.toLowerCase()) {
            case "technical" -> TECHNICAL_WORKER_PROMPT;
            case "documentation" -> DOCUMENTATION_WORKER_PROMPT;
            case "testing" -> TESTING_WORKER_PROMPT;
            default -> DEFAULT_WORKER_PROMPT;
        };
    }
}
