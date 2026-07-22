# STDIO MCP Server - Quick Start Guide

## 1. 빌드

```bash
cd stdio-mcp-server
./mvnw clean package
```

## 2. 실행

### 방법 1: Maven으로 실행
```bash
./mvnw spring-boot:run
```

### 방법 2: JAR로 실행
```bash
java -jar target/stdio-mcp-server-1.0.0.jar
```

### 방법 3: 스크립트로 실행
```bash
./run.sh
```

## 3. 접속

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **H2 Console**: http://localhost:8082/h2-console
- **Actuator Health**: http://localhost:8082/actuator/health
- **API Base URL**: http://localhost:8082/api

## 4. API 테스트

```bash
./test-api.sh
```

## 5. Claude Desktop 연동

### 빌드 후 등록
```bash
# 1. 프로젝트 빌드
./mvnw clean package

# 2. Claude Desktop에 등록
./register-claude.sh

# 3. Claude Desktop 재시작
```

### 수동 등록 (macOS)

Claude Desktop 설정 파일 위치:
```bash
~/Library/Application Support/Claude/claude_desktop_config.json
```

설정 내용:
```json
{
  "mcpServers": {
    "stdio-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/himang10/mydev/skala/skala-cloud/spring-ai-mcp/stdio-mcp-server/target/stdio-mcp-server-1.0.0.jar"
      ],
      "env": {
        "JAVA_HOME": "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
      }
    }
  }
}
```

**중요**: 
- JAR 파일 경로를 실제 경로로 수정하세요
- JAVA_HOME 경로를 시스템의 Java 21 경로로 수정하세요

### JAVA_HOME 확인 (macOS)
```bash
/usr/libexec/java_home -v 21
```

## 6. MCP Tools 사용 예시 (Claude Desktop에서)

### User Tools

```
모든 사용자 조회해줘
ID가 1인 사용자 정보 알려줘
이메일이 john@example.com인 사용자 찾아줘
이름에 "John"이 포함된 사용자 검색해줘
이름이 "Jane Doe"이고 이메일이 "jane@example.com"인 사용자 생성해줘
```

### Product Tools

```
모든 상품 조회해줘
ID가 1인 상품 정보 알려줘
"Laptop"이라는 이름의 상품 검색해줘
이름이 "Mouse", 설명이 "Wireless mouse", 가격이 25.99, 재고가 50인 상품 생성해줘
```

## 7. REST API 예시

### User API

```bash
# 사용자 생성
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'

# 모든 사용자 조회
curl http://localhost:8082/api/users

# ID로 사용자 조회
curl http://localhost:8082/api/users/1

# 이메일로 사용자 조회
curl http://localhost:8082/api/users/email/john@example.com

# 이름으로 사용자 검색
curl "http://localhost:8082/api/users/search?name=John"

# 사용자 수정
curl -X PUT http://localhost:8082/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"John Smith","email":"john.smith@example.com"}'

# 사용자 삭제
curl -X DELETE http://localhost:8082/api/users/1
```

### Product API

```bash
# 상품 생성
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"High-end laptop","price":1500.00,"stock":10}'

# 모든 상품 조회
curl http://localhost:8082/api/products

# ID로 상품 조회
curl http://localhost:8082/api/products/1

# 상품명으로 상품 검색
curl "http://localhost:8082/api/products/search?name=Laptop"

# 상품 수정
curl -X PUT http://localhost:8082/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Gaming Laptop","description":"High-end gaming laptop","price":2000.00,"stock":5}'

# 상품 삭제
curl -X DELETE http://localhost:8082/api/products/1
```

## 8. 문제 해결

### 빌드 오류
```bash
# Maven Wrapper 다운로드
./mvnw wrapper:wrapper

# 의존성 강제 업데이트
./mvnw clean install -U
```

### 포트 충돌 (8082)
`application.yaml`에서 포트 변경:
```yaml
server:
  port: 8083
```

### Claude Desktop 연결 확인

**macOS 로그 확인**:
```bash
tail -f ~/Library/Logs/Claude/mcp*.log
```

**Windows 로그 확인**:
```cmd
type %APPDATA%\Claude\logs\mcp*.log
```

### H2 데이터베이스 접속

1. http://localhost:8082/h2-console 접속
2. 설정:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User Name: `sa`
   - Password: (비워둠)
3. Connect 클릭

## 9. 개발 팁

### Hot Reload (Spring Boot DevTools)
`pom.xml`에 추가:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### 로그 레벨 변경
`application.yaml`에서:
```yaml
logging:
  level:
    root: WARN
    '[com.example.stdio]': DEBUG  # 디버그 모드 활성화
  file:
    name: logs/stdio-mcp-server.log
```

**중요**: STDIO 모드에서는 절대 `console` 출력을 활성화하지 마세요. STDOUT이 프로토콜 통신에 사용되므로 통신이 망가집니다.

### 데이터베이스 초기 데이터
`src/main/resources/data.sql` 파일 생성:
```sql
INSERT INTO users (name, email, created_at, updated_at) 
VALUES ('John Doe', 'john@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (name, description, price, stock, created_at, updated_at) 
VALUES ('Laptop', 'High-end laptop', 1500.00, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

## 10. 다음 단계

- [ ] 실제 데이터베이스 연동 (PostgreSQL, MySQL)
- [ ] 인증/인가 추가 (Spring Security)
- [ ] 페이징 및 정렬 기능 추가
- [ ] 검증 로직 강화 (Hibernate Validator)
- [ ] 통합 테스트 작성
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인 구성

## 참고 자료

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Swagger/OpenAPI](https://springdoc.org/)
