# STDIO MCP Server

STDIO 프로토콜을 사용하는 MCP 서버로, User와 Product 관리 도구를 제공합니다.

## STDIO vs Web Server 차이점

STDIO MCP 서버는 일반적인 웹 서버 방식과 다르게 동작합니다:

### STDIO 프로토콜의 특징
- **통신 방식**: HTTP 대신 표준 입출력(STDIN/STDOUT)을 통해 통신
- **프로세스 모델**: Claude Desktop이 서버 프로세스를 직접 실행하고 관리
- **포트 불필요**: 웹 서버를 실행하지 않으므로 포트 번호가 필요 없음
- **로그 제약**: STDOUT은 MCP 프로토콜 통신에 사용되므로 콘솔 로그 출력 불가

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.4.6
- **Spring AI**: 1.1.0
- **Spring Data JPA**: H2 In-Memory Database
- **MCP Protocol**: STDIO

## Maven 의존성 설정 (pom.xml)

### 필수 의존성

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring AI MCP Server (STDIO) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server</artifactId>
    </dependency>

    <!-- Spring AI Document Reader -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-jsoup-document-reader</artifactId>
    </dependency>

    <!-- H2 Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Jackson for JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 주요 포인트
- `spring-ai-starter-mcp-server`: STDIO 프로토콜 지원을 위한 필수 의존성
- `spring-ai-bom`: Spring AI 버전 관리를 위한 BOM
- Swagger, Actuator 의존성은 **제외**

## Application 설정 (application.yaml)

```yaml
spring:
  application:
    name: stdio-mcp-server
  
  main:
    web-application-type: none  # 웹 서버 비활성화 (STDIO 전용)
    banner-mode: log  # Spring Boot 배너를 로그로만 출력

  # H2 Database Configuration
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false  # SQL 로그를 콘솔에 출력하지 않음
    defer-datasource-initialization: true
  
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql

  # MCP Server Configuration (STDIO)
  ai:
    mcp:
      server:
        name: stdio-mcp-server
        version: 1.0.0
        instructions: "STDIO MCP Server providing User and Product management tools"
        stdio: true  # STDIO 프로토콜 활성화
        type: SYNC   # 동기 방식
        annotation-scanner:
          enabled: true  # MCP 어노테이션 자동 스캔

# Logging Configuration
logging:
  level:
    root: INFO
    '[org.springframework.ai]': DEBUG
    '[io.modelcontextprotocol]': DEBUG
    '[com.example.stdio]': DEBUG
  file:
    name: stdio-mcp-server.log  # 파일로만 로그 출력
```

### 핵심 설정 설명

1. **`web-application-type: none`**
   - 웹 서버를 완전히 비활성화
   - STDIO는 HTTP 포트가 필요 없음

2. **`spring.ai.mcp.server.stdio: true`**
   - STDIO 프로토콜 활성화 (Spring AI 1.1.0 공식)
   - 기존 `protocol: STDIO` 대신 사용

3. **`banner-mode: log`**
   - Spring Boot 배너를 STDOUT이 아닌 로그로만 출력
   - STDOUT은 MCP 프로토콜 통신 전용

4. **`show-sql: false`**
   - SQL 로그를 콘솔에 출력하지 않음
   - STDOUT 오염 방지

## 빌드 및 실행

### Maven 빌드

```bash
cd stdio-mcp-server
mvn clean package -DskipTests
```

### 로컬 실행 (테스트용)

```bash
java -jar target/stdio-mcp-server-1.0.0.jar
```

## MCP Tools

### User Tools (7개)
1. `getAllUsers` - 모든 사용자 조회
2. `getUserById` - ID로 사용자 조회
3. `getUserByEmail` - 이메일로 사용자 조회
4. `searchUsersByName` - 이름으로 사용자 검색
5. `createUser` - 사용자 생성
6. `updateUser` - 사용자 수정
7. `deleteUser` - 사용자 삭제

### Product Tools (6개)
1. `getAllProducts` - 모든 상품 조회
2. `getProductById` - ID로 상품 조회
3. `searchProductsByName` - 상품명으로 상품 검색
4. `createProduct` - 상품 생성
5. `updateProduct` - 상품 수정
6. `deleteProduct` - 상품 삭제

## Claude Desktop 연동

### 설정 방법

Claude Desktop의 설정 파일을 직접 편집하여 다음 내용을 추가합니다:

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`

**Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

### 설정 예시

```json
{
  "mcpServers": {
    "stdio-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/절대/경로/stdio-mcp-server/target/stdio-mcp-server-1.0.0.jar"
      ],
      "env": {
        "JAVA_HOME": "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
      }
    }
  }
}
```

### 설정 시 주의사항

1. **JAR 파일 경로**
   - 반드시 **절대 경로**를 사용하세요
   - 예: `/Users/yourname/projects/stdio-mcp-server/target/stdio-mcp-server-1.0.0.jar`

2. **JAVA_HOME 경로**
   - Java 21 설치 경로를 정확히 지정
   - macOS 예: `/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`
   - Windows 예: `C:\Program Files\Java\jdk-21`

3. **설정 완료 후**
   - Claude Desktop을 **완전히 종료** 후 재시작
   - 설정이 제대로 적용되었는지 확인

### 경로 확인 방법

**macOS/Linux**:
```bash
# Java 경로 확인
/usr/libexec/java_home -v 21

# JAR 파일 절대 경로 확인
cd stdio-mcp-server/target
pwd
# 출력된 경로 + /stdio-mcp-server-1.0.0.jar
```

**Windows**:
```cmd
# Java 경로 확인
where java

# JAR 파일 절대 경로 확인
cd stdio-mcp-server\target
cd
```

## 로깅

### STDIO 프로토콜과 로깅의 중요성

**핵심**: STDIO 프로토콜은 STDIN/STDOUT을 통해 Claude Desktop과 통신하므로, 콘솔로 출력되는 모든 로그가 프로토콜 통신을 방해합니다.

### 로그 설정
- **로그 파일**: `stdio-mcp-server.log`
- **로그 레벨**: INFO (Spring AI/MCP는 DEBUG)
- **콘솔 출력**: 비활성화 (파일로만 출력)

### 로그 확인

```bash
# 실시간 로그 확인
tail -f stdio-mcp-server.log

# 최근 100줄 확인
tail -n 100 stdio-mcp-server.log
```

## 문제 해결

### 1. Claude Desktop에서 서버가 보이지 않는 경우

- `claude_desktop_config.json` 파일 경로 확인
- JSON 문법 오류 확인 (쉼표, 중괄호 등)
- Claude Desktop 완전 재시작

### 2. 서버 시작 실패

- `stdio-mcp-server.log` 파일 확인
- JAVA_HOME 경로가 올바른지 확인
- JAR 파일이 존재하는지 확인

### 3. Claude Desktop 로그 확인

**macOS**:
```bash
tail -f ~/Library/Logs/Claude/mcp*.log
```

**Windows**:
```cmd
type %APPDATA%\Claude\logs\mcp*.log
```

## 참고 자료

- [Spring AI MCP Server Documentation](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/specification)

## 라이선스

Apache 2.0
