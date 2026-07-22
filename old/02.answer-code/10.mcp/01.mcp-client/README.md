

# mcp-client
Spring AI MCP(Model Context Protocol) 클라이언트를 통해 OpenAI와 통합하여 AI 기반 대화형 서비스를 제공하는 애플리케이션

## 목적

이 프로젝트는 Spring AI의 MCP 클라이언트를 활용하여 OpenAI GPT 모델과 통신하고, 외부 MCP 서버가 제공하는 도구(Tools)를 사용하여 확장 가능한 AI 서비스를 구현합니다. 사용자는 웹 인터페이스를 통해 AI와 대화하고, 다양한 MCP 서버의 도구를 활용할 수 있습니다.


## 기술 스펙

- **Java**: 21
- **Spring Boot**: 3.4.6
- **Spring AI**: 1.1.0
- **Gradle**: 8.x
- **주요 라이브러리**:
  - `spring-ai-starter-model-openai`: OpenAI 모델 통합
  - `spring-ai-starter-mcp-client`: MCP 클라이언트 기능
  - `spring-boot-starter-web`: RESTful API
  - `spring-boot-starter-webflux`: 리액티브 웹 지원
  - `spring-boot-starter-thymeleaf`: 템플릿 엔진
  - `lombok`: 코드 간소화
---
## 폴더 및 파일 구성

```
01.mcp-client/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── controller/
│   │   │   │   ├── AiController.java          # AI 대화 및 차량 인식 API
│   │   │   │   └── HomeController.java        # 홈페이지 렌더링
│   │   │   ├── service/
│   │   │   │   └── AiService.java             # AI 비즈니스 로직
│   │   │   └── DemoApplication.java           # 메인 애플리케이션
│   │   └── resources/
│   │       ├── application.yaml               # 애플리케이션 설정
│   │       ├── application.properties         # 프로젝트 이름 설정
│   │       └── templates/                     # Thymeleaf 템플릿
│   └── test/
│       └── java/com/example/demo/
│           └── DemoApplicationTests.java
├── build.gradle                               # Gradle 빌드 설정
└── README.md
```

### 주요 파일 설명

- **AiController.java**: AI와의 대화 및 이미지 기반 차량 번호판 인식 API 엔드포인트 제공
- **AiService.java**: ChatClient를 통해 OpenAI와 통신하고, MCP 서버의 도구를 활용
- **application.yaml**: OpenAI API 키, MCP 서버 연결 정보, 서버 포트 등 설정

## 빌드 및 실행 방법

### 1. 사전 요구사항

- Java 21 이상
- Gradle 8.x 이상
- OpenAI API 키 (application.yaml에 설정 필요)
- MCP 서버 실행 중 (STDIO 또는 HTTP)

### 2. 환경 변수 / 설정

**application.yaml** 주요 설정:

```yaml
spring:
  ai:
    openai:
      api-key: YOUR_OPENAI_API_KEY    # OpenAI API 키 (필수)
      chat:
        options:
          model: gpt-4o-mini            # 사용할 GPT 모델
    mcp:
      client:
        type: SYNC                      # MCP 클라이언트 타입
        # STDIO 로컬 서버
        stdio:
          connections:
            stdio-mcp-server:
              command: java
              args:
                - "-jar"
                - "/absolute/path/to/stdio-mcp-server-1.0.0.jar"
            kubernetes-mcp-server:
              command: npx
              args:
                - "-y"
                - "kubernetes-mcp-server@latest"
        # Streamable HTTP 원격 서버
        streamable-http:
          connections:
            observability-mcp-server:
              url: https://observability-mcp-server.skala25a.project.skala-ai.com
              endpoint: /mcp

server:
  port: 8080                             # 클라이언트 서버 포트
```

### 3. 빌드

```bash
./gradlew clean build
```

### 4. 실행

```bash
./gradlew bootRun
```

또는 JAR 파일 실행:

```bash
java -jar build/libs/mcp-client-0.0.1-SNAPSHOT.jar
```

### 5. 접속

브라우저에서 `http://localhost:8080` 접속

## MCP 서버 연결 구성

### STDIO 프로토콜 (로컬 실행)

STDIO MCP 서버는 클라이언트가 직접 프로세스를 실행하고 관리합니다:

```yaml
stdio:
  connections:
    stdio-mcp-server:
      command: java
      args:
        - "-jar"
        - "/Users/himang10/mydev/skala/skala-cloud/spring-ai-mcp/02-1.first-mcp-server/stdio-mcp-server/target/stdio-mcp-server-1.0.0.jar"
```

**특징**:
- 표준 입출력(STDIN/STDOUT)으로 통신
- 포트 번호 불필요
- 클라이언트가 서버 프로세스 생명주기 관리
- User/Product 관리 도구 제공 (7개 User Tools, 6개 Product Tools)

**사전 요구사항**:
```bash
cd /Users/himang10/mydev/skala/skala-cloud/spring-ai-mcp/02-1.first-mcp-server/stdio-mcp-server
./mvnw clean package -DskipTests
```

### Streamable HTTP 프로토콜 (원격 연결)

HTTP MCP 서버는 별도 프로세스로 실행되며 HTTP로 통신합니다:

```yaml
streamable-http:
  connections:
    observability-mcp-server:
      url: https://observability-mcp-server.skala25a.project.skala-ai.com
      endpoint: /mcp
```

**특징**:
- HTTP POST/GET 요청으로 통신
- 원격 서버 연결 가능
- SSE 스트리밍 지원
- Observability 도구 제공 (Prometheus 메트릭, 로그 조회 등)

**로컬 HTTP 서버 실행** (선택사항):
```bash
# SSE 프로파일 (포트 8081)
cd /Users/himang10/mydev/skala/skala-cloud/spring-ai-mcp/02-1.first-mcp-server/http-mcp-server
./mvnw clean package -DskipTests
java -jar target/http-mcp-server-1.0.0.jar --spring.profiles.active=sse
```

로컬 서버 사용 시 application.yaml 설정:
```yaml
streamable-http:
  connections:
    http-mcp-server:
      url: http://localhost:8081
      endpoint: /mcp
```

## 의존 관계

### 연동 가능한 MCP 서버

1. **stdio-mcp-server** (로컬 STDIO)
   - 위치: `02-1.first-mcp-server/stdio-mcp-server`
   - 프로토콜: STDIO
   - 도구: User/Product CRUD 관리

2. **http-mcp-server** (로컬 HTTP)
   - 위치: `02-1.first-mcp-server/http-mcp-server`
   - 프로토콜: SSE (8081), Streamable (8082), Stateless (8083)
   - 도구: User/Product CRUD 관리 + REST API

3. **kubernetes-mcp-server** (NPM 패키지)
   - 프로토콜: STDIO
   - 도구: Kubernetes 클러스터 관리

4. **observability-mcp-server** (원격 HTTP)
   - URL: https://observability-mcp-server.skala25a.project.skala-ai.com
   - 프로토콜: Streamable HTTP
   - 도구: Prometheus 메트릭, 로그 조회, 시스템 모니터링

## 주요 기능

1. **AI 대화**: 사용자 질문에 대해 GPT 모델이 답변 제공
2. **도구 자동 통합**: MCP 서버의 도구를 자동으로 감지하고 호출
   - STDIO 서버: User/Product 관리, Kubernetes 작업
   - HTTP 서버: Observability 데이터 조회, 메트릭 분석
3. **멀티 서버 지원**: 여러 MCP 서버를 동시에 연결하여 사용
4. **프로토콜 혼용**: STDIO와 HTTP 프로토콜을 동시에 사용 가능

## 로깅

디버그 모드 활성화:
- `io.modelcontextprotocol`: DEBUG
- `org.springframework.ai`: DEBUG

콘솔 로그 형식: `%-5level %logger.%M(): %msg%n`

## 참고사항

- OpenAI API 키는 보안상 환경 변수로 관리하는 것을 권장합니다
- STDIO MCP 서버는 JAR 파일이 빌드되어 있어야 합니다
- HTTP MCP 서버는 별도 프로세스로 실행 중이어야 연결 가능합니다
- 멀티파트 파일 업로드 제한: 최대 10MB (설정 변경 가능)

## MCP 프로토콜 비교

| 프로토콜 | 통신 방식 | 연결 대상 | 포트 | 프로세스 관리 | 사용 사례 |
|---------|----------|----------|-----|-------------|---------|
| **STDIO** | STDIN/STDOUT | 로컬 | 불필요 | 클라이언트가 관리 | 로컬 도구 통합 |
| **SSE** | HTTP + SSE 스트리밍 | 로컬/원격 | 8081 | 독립 실행 | 실시간 이벤트 |
| **Streamable** | HTTP POST/GET | 로컬/원격 | 8082 | 독립 실행 | 유연한 HTTP 통신 |
| **Stateless** | HTTP POST/GET | 로컬/원격 | 8083 | 독립 실행 | 클라우드 배포 |

## 참고 문서

- [Spring AI MCP Client Documentation](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/specification)
- [STDIO MCP Server README](../02-1.first-mcp-server/stdio-mcp-server/README.md)
- [HTTP MCP Server README](../02-1.first-mcp-server/http-mcp-server/README.md)

