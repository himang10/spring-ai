# Spring AI Advisors 샘플

Spring AI의 Advisor 기능을 데모하는 프로젝트입니다.

## 개요

이 프로젝트는 Spring AI의 Advisor 패턴을 보여줍니다. 특히 `JsonLoggingAdvisor`를 통해 LLM 요청과 응답을 JSON 형식으로 로깅하는 기능을 제공합니다.

## 주요 기능

- **JsonLoggingAdvisor**: LLM 요청/응답을 JSON 형식으로 로깅
- **ApplicationRunner**: 애플리케이션 시작 시 자동으로 advisor 데모 실행
- **ChatClient 통합**: Spring AI ChatClient와 advisor 통합

## 프로젝트 구조

```
src/main/java/com/example/springai/
├── SpringAiAdvisorsApplication.java  # 메인 애플리케이션
├── config/
│   └── ChatClientConfig.java         # ChatClient 설정 (advisor 포함)
├── advisor/
│   └── JsonLoggingAdvisor.java       # JSON 로깅 advisor
└── runner/
    └── AdvisorDemoRunner.java        # 자동 실행 데모
```

## Advisor란?

Advisor는 Spring AI에서 ChatClient의 요청/응답 처리 과정에 간섭할 수 있는 인터셉터 패턴입니다. 

### 주요 용도:
- 로깅 및 모니터링
- 요청/응답 변환
- 캐싱
- 오류 처리
- 보안 및 권한 검증

## JsonLoggingAdvisor

`JsonLoggingAdvisor`는 다음 정보를 JSON 형식으로 로깅합니다:

### 요청 로그:
```json
{
  "timestamp": "2024-01-20T10:30:00",
  "type": "CHAT_REQUEST",
  "systemMessage": "시스템 메시지",
  "userMessage": "사용자 질문",
  "options": {
    "model": "gpt-4o-mini",
    "temperature": 0.7
  }
}
```

### 응답 로그:
```json
{
  "timestamp": "2024-01-20T10:30:05",
  "type": "CHAT_RESPONSE",
  "content": "AI 응답 내용",
  "finishReason": "STOP",
  "metadata": {
    "model": "gpt-4o-mini",
    "promptTokens": 100,
    "completionTokens": 50,
    "totalTokens": 150
  }
}
```

## 실행 방법

### 1. 환경 변수 설정

```bash
export OPEN_AI_KEY=your-openai-api-key
```

### 2. 애플리케이션 실행

```bash
mvn spring-boot:run
```

### 3. 출력 확인

애플리케이션이 시작되면 자동으로 3가지 예제가 실행됩니다:
1. 단순 질문 (대한민국의 수도)
2. 배우의 필모그래피 (톰 행크스)
3. 리스트 요청 (아이스크림 맛)

각 예제의 요청과 응답이 JSON 형식으로 로깅됩니다.

## ChatClient 설정

`ChatClientConfig`에서 advisor를 등록합니다:

```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder, 
                             JsonLoggingAdvisor jsonLoggingAdvisor) {
    return builder
            .defaultSystem("시스템 프롬프트")
            .defaultAdvisors(jsonLoggingAdvisor)
            .build();
}
```

## 커스텀 Advisor 만들기

새로운 advisor를 만들려면 `CallAdvisor` 인터페이스를 구현하면 됩니다:

```java
@Component
public class MyCustomAdvisor implements CallAdvisor {
    
    @Override
    public AdvisedResponse adviseCall(AdvisedRequest advisedRequest, 
                                     CallAroundAdvisorChain chain) {
        // 요청 전처리
        // ...
        
        // 다음 advisor 또는 실제 호출
        AdvisedResponse response = chain.nextAroundCall(advisedRequest);
        
        // 응답 후처리
        // ...
        
        return response;
    }
    
    @Override
    public int getOrder() {
        return 0; // 실행 순서 (낮을수록 먼저 실행)
    }
}
```

## 기술 스택

- Spring Boot 3.4.6
- Spring AI 1.1.0
- Java 21
- OpenAI GPT-4o-mini

## 참고

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [Advisor 가이드](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_advisors)
