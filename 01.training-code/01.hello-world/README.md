# Spring AI Chat Sample Application

Spring AI 1.1.0을 활용한 ChatClient 기반 채팅 애플리케이션입니다.

## 📋 프로젝트 개요

이 프로젝트는 Spring AI를 활용한 교육용 샘플 코드로, 단계별로 기능을 확장할 수 있도록 설계되었습니다.

### 현재 단계: 1단계 - 기본 ChatClient 구현

- ✅ Spring AI ChatClient를 사용한 AI 대화 기능
- ✅ ChatGPT 스타일의 심플한 UI (Thymeleaf)
- ✅ 대화 이력 관리 (메모리 기반)
- ✅ 파일 업로드 기능 (PDF, TXT)
- ✅ 새 대화 시작 기능

### 향후 단계

2. **RAG 구현** - 업로드한 문서를 벡터 DB에 저장 및 검색
3. **PGVector 연동** - PostgreSQL 기반 벡터 저장소
4. **Qdrant 연동** - 클라우드 벡터 DB
5. **AI Agent 구현** - 멀티 에이전트 시스템

## 🚀 시작하기

### 필요 사항

- Java 17 이상
- Maven 3.6 이상
- OpenAI API Key

### 환경 변수 설정

OpenAI API Key를 환경 변수로 설정해야 합니다:

```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

### 빌드 및 실행

```bash
# 프로젝트 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

### 접속

브라우저에서 다음 주소로 접속:

```
http://localhost:8080
```

## 📁 프로젝트 구조

```
spring-ai-sample/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── springai/
        │               ├── SpringAiSampleApplication.java
        │               ├── config/
        │               │   └── ChatClientConfig.java      # ChatClient 설정
        │               ├── controller/
        │               │   ├── ChatController.java        # 채팅 API
        │               │   └── SettingsController.java    # 파일 업로드
        │               ├── service/
        │               │   └── ChatService.java           # 채팅 로직
        │               └── dto/
        │                   ├── ChatRequest.java
        │                   └── ChatResponse.java
        └── resources/
            ├── application.yml                             # 설정 파일
            ├── templates/
            │   └── chat.html                               # 채팅 UI
            └── static/
                ├── css/
                │   └── chat.css                            # 스타일
                └── js/
                    └── chat.js                             # 클라이언트 로직
```

## 🎯 주요 기능

### 1. ChatClient를 사용한 AI 대화

Spring AI 1.1.0의 `ChatClient`를 활용하여 OpenAI GPT 모델과 대화합니다.

### 2. 대화 이력 관리

각 대화 세션의 이력을 메모리에 저장하여 컨텍스트를 유지합니다.

### 3. 파일 업로드

PDF 및 텍스트 파일을 업로드할 수 있습니다 (다음 단계에서 RAG 구현에 활용).

### 4. ChatGPT 스타일 UI

- 실시간 타이핑 인디케이터
- 자동 스크롤
- 메시지 애니메이션
- 드래그 앤 드롭 파일 업로드

## 🔧 API 엔드포인트

### 채팅 API

- `POST /api/chat` - 메시지 전송 및 AI 응답 받기
- `POST /api/chat/new` - 새 대화 시작
- `DELETE /api/chat/{conversationId}` - 대화 이력 삭제

### 파일 업로드 API

- `POST /api/settings/upload` - 파일 업로드 (multipart/form-data)

## 🛠️ 기술 스택

- **Spring Boot 3.4.0**
- **Spring AI 1.1.0**
  - ChatClient
  - OpenAI Integration
  - PDF Document Reader (준비됨)
  - PGVector Store (준비됨)
  - Qdrant Store (준비됨)
- **Thymeleaf** - 템플릿 엔진
- **Lombok** - 코드 간소화
- **Maven** - 빌드 도구

## 📝 설정 커스터마이징

`application.yml`에서 다음 설정을 변경할 수 있습니다:

```yaml
spring:
  ai:
    openai:
      chat:
        options:
          model: gpt-4o-mini          # 사용할 모델
          temperature: 0.7             # 창의성 (0.0 ~ 1.0)
          max-tokens: 2000             # 최대 토큰 수
```

## 📚 다음 단계

이 코드를 기반으로 다음 기능들을 추가할 예정입니다:

1. **문서 임베딩** - 업로드한 파일을 벡터로 변환
2. **RAG 구현** - 문서 기반 질의응답
3. **벡터 DB 연동** - PGVector와 Qdrant 연동
4. **AI Agent** - 멀티 에이전트 시스템 구축

## 📄 라이선스

이 프로젝트는 교육 목적으로 제공됩니다.
