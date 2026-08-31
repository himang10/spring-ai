# STDIO + HTTP(Streamable) 통합 MCP Server 실습

## 실습 개요

Spring Boot 기반 API Service(Controller → Service → Repository 구조)에
**MCP(Model Context Protocol) Tool**을 추가로 노출하며, `@RestController`는 삭제하지 않고
그대로 유지한 채 `tool` 패키지의 `@McpTool` 메서드가 병행 동작합니다.

- 통신 방식(프로필) 3종 — 실행 시 `--spring.profiles.active` 값으로 택일합니다.
  - `stdio` : 표준 입출력(STDIO) 기반 MCP. 내장 웹 서버(Tomcat)를 아예 띄우지 않습니다.
  - `stateful-http` : Streamable HTTP, 세션(`MCP-Session-Id`)을 유지합니다.
  - `stateless-http` : Streamable HTTP, 세션을 유지하지 않습니다(요청마다 독립 처리, 수평 확장에 유리).
- 대상 도메인: User, Product (Controller/Service/Repository/Tool은 세 프로필 모두 공통으로 재사용)
- 데이터 저장소: H2 In-Memory Database
- `application.yaml`에는 기본 활성 프로필이 지정되어 있지 않으므로, 실행할 때 반드시
  `--spring.profiles.active=stdio|stateful-http|stateless-http` 중 하나를 명시해야 합니다.

## 실습 목표

하나의 Spring Boot 프로젝트에서 **전송 계층(STDIO ↔ Streamable HTTP)만 프로필로 갈아끼우고**,
나머지 도메인 로직(Controller/Service/Repository/Tool)은 그대로 재사용하는 구조를 이해합니다.

1. **tool 패키지 추가**: 기존 `@RestController`가 처리하던 기능과 동일한 동작을 하는
   `@McpTool` 메서드를 추가 작성 (Controller는 삭제하지 않고 그대로 유지)
2. **프로필별 application 설정 분리**: 공통 설정(`application.yaml`)과 전송 계층별 설정
   (`application-stdio.yaml`, `application-stateful-http.yaml`, `application-stateless-http.yaml`)을
   분리하여, 실행 시 프로필 하나만 바꿔 끼우면 통신 방식이 전환되도록 구성

즉, 별도 프로젝트로 나뉘어 있던 STDIO 버전과 HTTP 버전의 **공통 코드(도메인/Controller/Tool)는
하나로 합치고, 서로 다른 부분(전송 계층 설정)만 프로필로 분리**한 것이 이번 통합의 핵심입니다.

## 기술 스택

| 구분 | 내용 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| AI Framework | Spring AI 2.0.x (MCP Server) |
| 통신 프로토콜 | MCP over STDIO / Streamable HTTP (STATEFUL, STATELESS) |
| Database | H2 (In-Memory) |
| ORM | Spring Data JPA |
| API 문서화 | springdoc-openapi (Swagger UI, HTTP 프로필에서만 사용) |
| 빌드 도구 | Gradle / Maven (둘 다 지원) |
| 기타 | Lombok, Spring Boot Actuator |

## 프로젝트 구조

```
10-3.my-http-mcp-server
├── build.gradle / pom.xml
├── claude_desktop_config.json      # Claude Desktop 연동 설정 예시 (mcp-remote 브릿지)
└── src/main
    ├── java/com/example/http
    │   ├── HttpMcpServerApplication.java
    │   ├── domain/         # User, Product 엔티티 (모든 프로필 공통)
    │   │   ├── User.java
    │   │   └── Product.java
    │   ├── repository/     # Spring Data JPA Repository (모든 프로필 공통)
    │   │   ├── UserRepository.java
    │   │   └── ProductRepository.java
    │   ├── service/        # 비즈니스 로직 (모든 프로필 공통)
    │   │   ├── UserService.java
    │   │   └── ProductService.java
    │   ├── controller/      # 기존 REST API: 삭제하지 않고 그대로 유지
    │   │   ├── UserController.java
    │   │   └── ProductController.java
    │   └── tool/            # REST API와 별개로 노출되는 MCP Tool 계층
    │       ├── UserTools.java
    │       └── ProductTools.java
    └── resources
        ├── application.yaml               # 공통 설정(DB, Actuator, Swagger, 로깅 등)
        ├── application-stdio.yaml         # stdio 프로필: 웹 서버 비활성화 + STDIO 전송
        ├── application-stateful-http.yaml # stateful-http 프로필: 세션 유지 Streamable HTTP
        ├── application-stateless-http.yaml# stateless-http 프로필: 세션 미유지 Streamable HTTP
        └── data.sql
```

## 기존 Spring Boot 프로젝트에서 무엇을 바꿔야 하는가

### 1. 의존성 추가

STDIO와 Streamable HTTP 모두 **동일한 의존성**을 사용합니다. 전송 방식은
런타임 설정(`application-{profile}.yaml`)에서 결정되며, 의존성 자체는 프로필과 무관하게 하나만 있으면 됩니다.

**Gradle (`build.gradle`)**

```groovy
ext {
    springAiVersion = '2.0.0'
}

dependencies {
    // 기존 의존성은 유지
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // MCP Server 의존성 추가 (STDIO/HTTP 공통 starter)
    implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'
}

dependencyManagement {
    imports { mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}" }
}
```

**Maven (`pom.xml`)**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>2.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- MCP Server 의존성 추가 (STDIO/HTTP 공통 starter) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    </dependency>
</dependencies>
```

> `spring-ai-starter-mcp-server-webmvc` 하나로 STDIO와 Streamable HTTP(STATEFUL/STATELESS)를
> 모두 지원하며, 실제 전송 방식은 활성화된 프로필의 `spring.ai.mcp.server.stdio` /
> `spring.ai.mcp.server.protocol` 값에 따라 결정됩니다.

### 2. 프로필별 application 설정 분리

전송 계층 설정을 **공통 설정(`application.yaml`)** 과 **프로필 전용 설정** 3개 파일로 분리했습니다.
실행 시 `--spring.profiles.active` 값으로 세 파일 중 하나를 선택해 활성화합니다.

**공통 설정 (`application.yaml`)**

```yaml
server:
  port: 8081

spring:
  application:
    name: http-mcp-server

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    defer-datasource-initialization: true

  sql:
    init:
      mode: always
      data-locations: classpath:data.sql

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

**`stdio` 프로필 (`application-stdio.yaml`, 표준 입출력)**

```yaml
spring:
  main:
    # STDIO는 표준 입출력으로 통신하므로 내장 웹 서버(Tomcat)를 띄우지 않는다.
    web-application-type: none
    # 배너를 표준출력(stdout)에 찍으면 JSON-RPC 메시지 스트림에 섞여 클라이언트 파싱 에러가 나므로 끈다.
    banner-mode: off

  ai:
    mcp:
      server:
        name: my-spring-ai-mcp-server
        version: 1.0.0
        annotation-scanner:
          enabled: true
        # protocol 속성은 사용하지 않는다. stdio=true 이면 protocol 설정은 무시된다.
        stdio: true
        type: SYNC

logging:
  # 콘솔에 로그가 한 줄이라도 섞이면 클라이언트가 JSON-RPC 메시지로 오인해 파싱 에러를 낸다.
  # 별도의 logback-spring.xml 없이 콘솔 임계값만 OFF로 올려 콘솔 출력을 완전히 끈다.
  threshold:
    console: OFF
  file:
    name: logs/mcp-server-stdio.log
```

> 콘솔 출력을 완전히 끄기 위해 커스텀 `logback-spring.xml`을 따로 둘 필요는 없습니다.
> Spring Boot 기본 로깅 설정은 `logging.file.name`이 지정되면 콘솔/파일 어펜더를 모두 등록하는데,
> `logging.threshold.console: OFF`를 주면 `ThresholdFilter`가 모든 레벨의 로그를 걸러내어
> 콘솔 어펜더만 사실상 무음 처리되고 파일 어펜더는 그대로 동작합니다.

**`stateful-http` 프로필 (`application-stateful-http.yaml`, 세션 상태 유지)**

```yaml
spring:
  ai:
    mcp:
      server:
        name: my-spring-ai-mcp-server
        version: 1.0.0
        annotation-scanner:
          enabled: true
        # 세션 상태를 유지하는 Streamable HTTP. 엔드포인트는 기본값 POST /mcp 를 사용한다.
        protocol: STREAMABLE
        type: SYNC
```

**`stateless-http` 프로필 (`application-stateless-http.yaml`, 세션 상태 미유지)**

```yaml
spring:
  ai:
    mcp:
      server:
        name: my-spring-ai-mcp-server
        version: 1.0.0
        annotation-scanner:
          enabled: true
        # 세션 상태를 보관하지 않는 Streamable HTTP. 요청마다 독립적으로 처리되어
        # 여러 인스턴스로 수평 확장하기 쉽다(클라우드 네이티브 배포에 적합).
        protocol: STATELESS
        type: SYNC
```

> `stdio` 프로필에서 설정한 `web-application-type: none`, `banner-mode: off`는
> `stateful-http`/`stateless-http` 프로필에서는 필요 없습니다. <br>
> HTTP 프로필은 내장 웹 서버를 그대로 사용하므로 Swagger UI, H2 Console, Actuator 등 기존 웹 기능과 자유롭게 공존합니다. <br>
> 두 HTTP 프로필의 차이는 서버가 세션(`MCP-Session-Id`)을 유지하는지 여부이며, `STATELESS` 서버는
> 클라이언트로의 역방향 요청(elicitation, sampling, ping)을 지원하지 않습니다.

### 3. tool 패키지 추가 (Controller는 유지)

프로필과 무관하게 `@RestController`를 **삭제하지 않고 그대로 둔 채**, 동일한 기능을 수행하는
`@McpTool` 메서드를 `tool` 패키지에 별도로 작성합니다. Service/Repository는 Controller와
Tool 양쪽 계층에서 동일하게 주입받아 재사용합니다.

| 기존 (REST API, 유지) | 신규 추가 (MCP Tool) |
|---|---|
| `@RestController` | `@Component` |
| `@GetMapping("/api/products")` | `@McpTool(description = "...")` |
| `@RequestParam` / `@PathVariable` | `@McpToolParam(description = "...", required = true)` |
| `ResponseEntity<T>` 반환 | 도메인 객체/문자열을 그대로 반환 |

예시 (`ProductTools.java`, `ProductController.java`와 동일한 로직을 병행 제공):

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductTools {

    private final ProductService productService; // 기존 Service 그대로 주입

    @McpTool(description = "모든 상품 정보를 조회합니다.")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @McpTool(description = "ID로 특정 상품을 조회합니다.")
    public Product getProductById(
            @McpToolParam(description = "상품 ID", required = true) Long id
    ) {
        return productService.getProduct(id);
    }
}
```

- `@McpTool(description = ...)`: 해당 메서드를 MCP Tool로 등록하고, LLM에게 이 도구가
  무엇을 하는지 설명하는 메타데이터를 제공합니다.
- `@McpToolParam(description = ..., required = ...)`: 파라미터 각각에 대한 설명과
  필수 여부를 정의하여 LLM이 올바른 인자를 채워 호출할 수 있게 합니다.
- 클래스 자체는 `@RestController`가 아닌 일반 `@Component`이면 되고, MCP 서버가
  `annotation-scanner.enabled: true` 설정에 따라 `@McpTool` 메서드를 자동으로 스캔합니다.
- 기존 `ProductController`는 그대로 남아 있으므로, 같은 서버가 `/api/products`(REST)와
  `/mcp`(MCP) 양쪽으로 동일한 기능을 제공합니다.


## 빌드 및 실행

실행 전에 먼저 빌드하고, 빌드된 jar를 `--spring.profiles.active` 옵션과 함께 실행합니다.
Gradle 프로젝트 이름이 `10-3.my-http-mcp-server`(디렉터리명)이므로 Gradle과 Maven의 산출물 jar
파일명이 다른 점에 유의하세요.

### Gradle

```bash
./gradlew clean build
# 산출물: build/libs/10-3.my-http-mcp-server-1.0.0.jar

# stdio 프로필 실행
java -jar build/libs/10-3.my-http-mcp-server-1.0.0.jar --spring.profiles.active=stdio

# stateful-http 프로필 실행 (세션 유지)
java -jar build/libs/10-3.my-http-mcp-server-1.0.0.jar --spring.profiles.active=stateful-http

# stateless-http 프로필 실행 (세션 미유지)
java -jar build/libs/10-3.my-http-mcp-server-1.0.0.jar --spring.profiles.active=stateless-http
```

### Maven

```bash
./mvnw clean package
# 산출물: target/http-mcp-server-1.0.0.jar

# stdio 프로필 실행
java -jar target/http-mcp-server-1.0.0.jar --spring.profiles.active=stdio

# stateful-http 프로필 실행 (세션 유지)
java -jar target/http-mcp-server-1.0.0.jar --spring.profiles.active=stateful-http

# stateless-http 프로필 실행 (세션 미유지)
java -jar target/http-mcp-server-1.0.0.jar --spring.profiles.active=stateless-http
```

> `./gradlew`/`./mvnw` wrapper 실행 파일이 없거나 동작하지 않는 환경이라면, 시스템에 설치된
> `gradle build -x test` / `mvn clean package -DskipTests` 명령을 대신 사용해도 동일한 결과물이 생성됩니다.

> IDE에서 바로 실행할 경우에는 Run Configuration의 VM/Program 옵션에
> `--spring.profiles.active=stdio` 처럼 프로필을 지정하거나, 환경변수
> `SPRING_PROFILES_ACTIVE=stdio`를 설정하세요. 프로필을 지정하지 않으면 MCP 서버가
> 전송 방식을 결정하지 못해 정상 동작하지 않습니다.

`stateful-http`/`stateless-http` 프로필로 실행하면 아래 엔드포인트를 사용할 수 있습니다.
(기본 포트 `8081`. `stdio` 프로필은 내장 웹 서버가 없으므로 HTTP 엔드포인트가 존재하지 않습니다.)

| 용도 | URL |
|---|---|
| MCP 엔드포인트 | `http://localhost:8081/mcp` |
| REST API (Product) | `http://localhost:8081/api/products` |
| Swagger UI | `http://localhost:8081/swagger-ui.html` |
| H2 Console | `http://localhost:8081/h2-console` |
| Actuator | `http://localhost:8081/actuator` |

`stdio` 프로필로 실행하면 콘솔에는 아무 로그도 출력되지 않으며(JSON-RPC 메시지와 섞이지 않도록),
대신 `logs/mcp-server-stdio.log` 파일에서 로그를 확인할 수 있습니다.


## Spring AI MCP Client(10-1.mcp-client) 연동

같은 저장소의 `10-1.mcp-client` 프로젝트(Spring AI MCP Client)에서도 이 서버에 접속할 수
있습니다. 연동 방식은 이 서버를 어떤 프로필로 기동했는지에 따라 달라집니다.

- `stdio` 프로필로 기동한 서버 → 클라이언트가 `spring.ai.mcp.client.stdio.connections`에
  `command`/`args`를 등록해 서버 프로세스를 자식 프로세스로 직접 실행
- `stateful-http`/`stateless-http` 프로필로 기동한 서버 → 클라이언트가
  `spring.ai.mcp.client.streamable-http.connections`에 서버 URL을 등록해 접속

```yaml
spring:
  ai:
    mcp:
      client:
        name: my-spring-ai-mcp-client
        version: 1.0.0
        type: SYNC
        streamable-http:
          connections:
            # 10-3.my-http-mcp-server(User/Product MCP Tool) 연결
            tool-server:
              url: http://localhost:8081
              endpoint: /mcp
```

- HTTP 프로필로 연동할 때는 **서버가 먼저 기동되어 있어야** 하며, 클라이언트는 `url`/`endpoint`로
  접속만 합니다. stdio 프로필로 연동할 때는 클라이언트가 서버 프로세스를 직접 실행하므로 서버를
  미리 띄워둘 필요가 없습니다.
- 연결 전 반드시 `./gradlew build` 또는 `./mvnw package`로 `10-3.my-http-mcp-server`를 빌드해
  두어야 하며, HTTP 프로필로 연동할 경우 `java -jar ... --spring.profiles.active=stateful-http`
  (또는 `stateless-http`)로 서버를 먼저 기동해 두어야 합니다.

등록 후 `10-1.mcp-client`를 실행하면 User/Product 관련 도구들이 클라이언트에서 자동으로
인식되어 호출할 수 있습니다.
