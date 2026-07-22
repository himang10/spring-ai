# Orchestrator-Workers Pattern with Spring AI

Spring AI를 사용한 Orchestrator-Workers 패턴 구현 예제입니다.

## ⚠️ 중요: Advisor vs Agentic Patterns

Spring AI에서는:
- **ReAct, RAG 등**: Built-in Advisor로 제공 ✅
- **Orchestrator-Workers**: 패턴만 제공 (Built-in 없음) ❌

이 프로젝트는 **두 가지 구현 방식**을 제공합니다:
1. **직접 워크플로우 방식**: Spring AI 문서의 패턴 그대로
2. **Advisor 통합 방식**: 다른 Advisor들과 함께 사용 가능

자세한 비교는 [ADVISOR_COMPARISON.md](ADVISOR_COMPARISON.md)를 참조하세요.

## 📋 개요

Orchestrator-Workers 패턴은 복잡한 작업을 효과적으로 처리하기 위한 AI 에이전트 패턴입니다:

1. **Orchestrator**: 복잡한 작업을 분석하고 서브작업들로 분해
2. **Workers**: 각 서브작업을 전문 분야에 맞게 병렬로 처리
3. **통합**: 모든 결과를 종합하여 최종 응답 생성

## 🏗️ 아키텍처

```
사용자 요청 
    ↓
Orchestrator (작업 분석 및 분해)
    ↓
[Worker 1] [Worker 2] [Worker 3] ... (병렬 처리)
    ↓
결과 통합 및 최종 응답
```

## 📁 프로젝트 구조

```
src/main/java/com/example/springai/
├── OrchestratorWorkerApplication.java  # 메인 애플리케이션
├── controller/
│   └── OrchestratorController.java     # REST API 컨트롤러
├── workflow/
│   ├── Orchestrator.java               # 작업 분석 및 분해
│   ├── Worker.java                     # 서브작업 처리
│   └── OrchestratorWorkersWorkflow.java # 전체 워크플로우 통합
└── model/
    ├── OrchestratorResponse.java       # Orchestrator 응답 모델
    └── WorkerResponse.java             # Worker 응답 모델
```

## 🚀 실행 방법

### 1. 환경 설정

OpenAI API 키를 환경 변수로 설정:

```bash
export OPENAI_API_KEY=your-api-key-here
```

또는 `application.yml`에서 직접 설정:

```yaml
spring:
  ai:
    openai:
      api-key: your-api-key-here
```

### 2. 애플리케이션 실행

```bash
mvn spring-boot:run
```

### 3. API 테스트

#### 예제 1: GET 요청 (기본 예제)

```bash
curl http://localhost:8080/api/orchestrator/example
```

#### 예제 2: POST 요청 (커스텀 작업)

```bash
curl -X POST http://localhost:8080/api/orchestrator/process \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Create a comprehensive guide for building a microservices architecture with Spring Boot"
  }'
```

## 💡 사용 사례

이 패턴은 다음과 같은 경우에 유용합니다:

- ✅ 서브작업을 사전에 예측할 수 없는 복잡한 작업
- ✅ 다양한 접근 방식이나 관점이 필요한 작업
- ✅ 적응적 문제 해결이 필요한 상황

예시:
- 기술 문서와 사용자 가이드 동시 생성
- 다양한 관점에서 코드 리뷰 수행
- 복합적인 비즈니스 분석 작업

## 🔧 주요 기능

### Orchestrator
- LLM을 사용하여 작업을 지능적으로 분석
- 작업을 전문화된 서브작업으로 분해
- 각 서브작업에 적합한 전문가 타입 지정

### Worker
- 전문 분야별 프롬프트 템플릿 (technical, documentation, testing 등)
- Java 21 Virtual Threads를 사용한 병렬 처리
- 확장 가능한 전문가 타입 시스템

### Workflow
- Orchestrator와 Worker의 원활한 통합
- 실시간 진행 상황 로깅
- 구조화된 최종 결과 생성

## 📊 응답 구조

```json
{
  "analysis": "작업에 대한 Orchestrator의 분석",
  "workerResponses": [
    {
      "taskType": "technical",
      "taskDescription": "서브작업 설명",
      "result": "Worker의 처리 결과"
    }
  ],
  "finalSummary": "모든 결과의 종합 요약"
}
```

## 🎯 장점

1. **확장성**: 새로운 전문가 타입을 쉽게 추가 가능
2. **효율성**: 병렬 처리로 처리 시간 단축
3. **유연성**: 다양한 유형의 복잡한 작업 처리 가능
4. **모듈성**: 각 컴포넌트가 독립적으로 테스트 및 유지보수 가능

## 🔗 참고 자료

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/index.html)
- [Building Effective Agents](https://docs.spring.io/spring-ai/reference/api/effective-agents.html)
- [Anthropic Research: Building Effective Agents](https://www.anthropic.com/research/building-effective-agents)

## 📝 라이선스

이 프로젝트는 예제 코드로 제공됩니다.
