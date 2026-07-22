# STDIO MCP Server - Build Success

## 수정 완료 사항

### 1. Spring AI 의존성 수정
- ✅ **spring-ai-starter-mcp-server**: STDIO 서버용 올바른 의존성 적용
- ✅ Spring AI 1.1.0 BOM을 통한 버전 관리
- ✅ 불필요한 의존성 제거 (swagger, webmvc 등)

### 2. application.yaml 설정 수정
Spring AI 공식 문서에 따라 STDIO 프로토콜 설정 변경:
```yaml
spring:
  ai:
    mcp:
      server:
        stdio: true  # STDIO 프로토콜 활성화
        type: SYNC   # 동기 방식
        annotation-scanner:
          enabled: true
```

기존 `protocol: STDIO` → `stdio: true`로 변경

### 3. 불필요한 파일 제거
STDIO 모드에서는 웹 서버 없이 STDIN/STDOUT으로 통신하므로 다음 파일 삭제:
- ✅ SwaggerConfig.java (Swagger는 HTTP 기반)
- ✅ UserController.java (REST API 불필요)
- ✅ ProductController.java (REST API 불필요)

### 4. 빌드 성공
```bash
./mvnw clean install -DskipTests
```
✅ BUILD SUCCESS

## 프로젝트 구조

### Core Files (유지)
- ✅ Domain: User.java, Product.java
- ✅ Repository: UserRepository.java, ProductRepository.java
- ✅ Service: UserService.java, ProductService.java
- ✅ Tools: UserTools.java, ProductTools.java (MCP 어노테이션 기반)
- ✅ Application: StdioMcpServerApplication.java

### Database
- ✅ H2 in-memory database
- ✅ data.sql: 5명의 사용자, 5개의 제품 초기 데이터

## MCP Tools 제공

### User Tools (7개)
1. `getAllUsers()` - 모든 사용자 조회
2. `getUserById(Long id)` - ID로 사용자 조회
3. `getUserByEmail(String email)` - 이메일로 사용자 조회
4. `searchUsersByName(String name)` - 이름으로 사용자 검색
5. `createUser(String name, String email)` - 사용자 생성
6. `updateUser(Long id, String name, String email)` - 사용자 수정
7. `deleteUser(Long id)` - 사용자 삭제

### Product Tools (6개)
1. `getAllProducts()` - 모든 제품 조회
2. `getProductById(Long id)` - ID로 제품 조회
3. `searchProductsByName(String name)` - 이름으로 제품 검색
4. `createProduct(String name, String description, Double price, Integer stock)` - 제품 생성
5. `updateProduct(Long id, String name, String description, Double price, Integer stock)` - 제품 수정
6. `deleteProduct(Long id)` - 제품 삭제

## 사용 방법

### 1. 빌드
```bash
cd stdio-mcp-server
./mvnw clean package -DskipTests
```

### 2. 실행
```bash
java -jar target/stdio-mcp-server-1.0.0.jar
```

### 3. Claude Desktop 연동
`claude_desktop_config.json` 파일을 Claude Desktop 설정에 추가하여 사용

## 참고 자료

- [Spring AI MCP Server Boot Starter Documentation](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- Spring AI 1.1.0 공식 문서에 따른 설정 적용
- STDIO 프로토콜: `spring.ai.mcp.server.stdio=true` 사용
