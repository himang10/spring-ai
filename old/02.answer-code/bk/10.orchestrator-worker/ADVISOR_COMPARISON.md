# Orchestrator-Workers Pattern 구현 비교

## Spring AI에서의 Advisor vs Agentic Patterns

### Built-in Advisors (Spring AI 제공)
- ✅ `QuestionAnswerAdvisor` - RAG 패턴
- ✅ `MessageChatMemoryAdvisor` - 대화 기록
- ✅ `VectorStoreChatMemoryAdvisor` - Vector Store 메모리
- ✅ `SimpleLoggerAdvisor` - 로깅

### Agentic Patterns (패턴만 제공, 직접 구현 필요)
- ❌ Chain Workflow
- ❌ Parallelization Workflow
- ❌ Routing Workflow
- ❌ **Orchestrator-Workers** (이 프로젝트)
- ❌ Evaluator-Optimizer

## 이 프로젝트의 두 가지 구현 방식

### 1. 직접 워크플로우 방식 (패턴 그대로)
```java
// Spring AI 문서의 예제 패턴을 따름
OrchestratorWorkersWorkflow workflow = new OrchestratorWorkersWorkflow(orchestrator, worker);
WorkerResponse response = workflow.process(taskDescription);
```

**장점:**
- 명확하고 직관적인 제어 흐름
- 디버깅이 쉬움
- Spring AI 문서의 패턴과 일치

**사용 시나리오:**
- 독립적으로 Orchestrator-Workers 패턴만 사용할 때
- 복잡한 커스터마이징이 필요할 때

### 2. Advisor 통합 방식 (새로 추가)
```java
// ChatClient의 advisor chain에 통합
chatClient.prompt()
    .advisors(new OrchestratorWorkersAdvisor(orchestrator, worker))
    .user(taskDescription)
    .call()
    .content();
```

**장점:**
- 다른 Advisor들(RAG, Memory 등)과 함께 사용 가능
- ChatClient의 표준 패턴 활용
- Advisor chain의 순서 제어 가능

**사용 시나리오:**
- RAG + Orchestrator-Workers 조합
- Memory + Orchestrator-Workers 조합
- 여러 Advisor를 조합할 때

## 프로젝트 구조 (업데이트)

```
src/main/java/com/example/springai/
├── OrchestratorWorkerApplication.java
├── advisor/
│   └── OrchestratorWorkersAdvisor.java     # ✨ 새로 추가: Advisor 구현
├── controller/
│   ├── OrchestratorController.java         # 기존: 직접 워크플로우 방식
│   └── AdvisorBasedController.java         # ✨ 새로 추가: Advisor 방식
├── workflow/
│   ├── Orchestrator.java
│   ├── Worker.java
│   └── OrchestratorWorkersWorkflow.java
└── model/
    ├── OrchestratorResponse.java
    └── WorkerResponse.java
```

## API 엔드포인트 비교

### 직접 워크플로우 방식
```bash
# 기존 방식
curl -X POST http://localhost:8080/api/orchestrator/process \
  -H "Content-Type: application/json" \
  -d '{"task": "Create a REST API guide"}'
```

### Advisor 통합 방식
```bash
# Advisor 방식 (자동으로 Orchestrator-Workers 실행)
curl -X POST http://localhost:8080/api/advisor/process \
  -H "Content-Type: application/json" \
  -d '{"task": "Create a REST API guide"}'

# Advisor 비활성화
curl -X POST http://localhost:8080/api/advisor/process-direct \
  -H "Content-Type: application/json" \
  -d '{"task": "Create a REST API guide"}'
```

## 고급 사용 예시: Advisor 조합

```java
// RAG + Orchestrator-Workers 조합
chatClient.prompt()
    .advisors(
        new QuestionAnswerAdvisor(vectorStore),              // 먼저 관련 문서 검색
        new OrchestratorWorkersAdvisor(orchestrator, worker) // 그 다음 작업 분해 및 처리
    )
    .user("복잡한 기술 문서 작성")
    .call()
    .content();

// Memory + Orchestrator-Workers 조합
chatClient.prompt()
    .advisors(
        new MessageChatMemoryAdvisor(chatMemory),            // 대화 이력 추가
        new OrchestratorWorkersAdvisor(orchestrator, worker) // 작업 분해 및 처리
    )
    .user("이전 대화를 바탕으로 새 작업 수행")
    .call()
    .content();
```

## 권장 사항

### 직접 워크플로우 방식 사용 시
- Orchestrator-Workers 패턴만 단독으로 사용
- 세밀한 제어가 필요한 경우
- 결과 구조가 명확히 필요한 경우 (WorkerResponse)

### Advisor 방식 사용 시
- 다른 Advisor와 조합 필요
- ChatClient의 표준 패턴 활용
- 동적으로 Advisor 활성화/비활성화 필요

## 결론

Spring AI는 **ReAct, RAG 등은 Built-in Advisor**로 제공하지만, **Orchestrator-Workers는 패턴만 제공**합니다. 

이 프로젝트는 두 가지 방식을 모두 제공:
1. **직접 워크플로우**: Spring AI 문서의 패턴 그대로
2. **Advisor 통합**: 다른 Advisor들과 함께 사용 가능한 확장

필요에 따라 적절한 방식을 선택하시면 됩니다! 🚀
