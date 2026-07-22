# STDIO MCP Server

STDIO 프로토콜 기반의 Model Context Protocol (MCP) 서버로, User와 Product 관리를 위한 도구를 제공합니다.

## ✨ 주요 기능

- **STDIO MCP Protocol**: Claude Desktop과 직접 통신
- **User Management**: 사용자 생성, 조회, 검색, 수정, 삭제
- **Product Management**: 상품 생성, 조회, 검색, 수정, 삭제
- **REST API**: Swagger UI를 통한 API 테스트
- **H2 Database**: 인메모리 데이터베이스 (개발용)
- **Actuator**: Health check, Metrics

## 🚀 빠른 시작

```bash
# 1. 빌드
./mvnw clean package

# 2. 실행
java -jar target/stdio-mcp-server-1.0.0.jar

# 3. Swagger UI 접속
open http://localhost:8082/swagger-ui.html
```

## 📦 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 21 | 런타임 |
| Spring Boot | 3.4.6 | 프레임워크 |
| Spring AI | 1.1.0 | MCP 서버 |
| Spring Data JPA | 3.4.6 | 데이터 액세스 |
| H2 Database | Runtime | 인메모리 DB |
| Springdoc OpenAPI | 2.6.0 | API 문서 |
| Lombok | Latest | 코드 간소화 |

## 🔧 프로젝트 구조

```
stdio-mcp-server/
├── src/
│   ├── main/
│   │   ├── java/com/example/stdio/
│   │   │   ├── StdioMcpServerApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── User.java
│   │   │   │   └── Product.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── ProductRepository.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   └── ProductService.java
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java
│   │   │   │   └── ProductController.java
│   │   │   ├── tool/
│   │   │   │   ├── UserTools.java      # MCP Tools
│   │   │   │   └── ProductTools.java   # MCP Tools
│   │   │   └── config/
│   │   │       └── SwaggerConfig.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
├── pom.xml
├── README.md
├── QUICK_START.md
├── run.sh
├── test-api.sh
├── register-claude.sh
└── claude_desktop_config.json
```

## 🎯 MCP Tools

### User Tools
| Tool | 설명 |
|------|------|
| `getAllUsers` | 모든 사용자 조회 |
| `getUserById` | ID로 사용자 조회 |
| `getUserByEmail` | 이메일로 사용자 조회 |
| `searchUsersByName` | 이름으로 사용자 검색 |
| `createUser` | 사용자 생성 |
| `updateUser` | 사용자 수정 |
| `deleteUser` | 사용자 삭제 |

### Product Tools
| Tool | 설명 |
|------|------|
| `getAllProducts` | 모든 상품 조회 |
| `getProductById` | ID로 상품 조회 |
| `searchProductsByName` | 상품명으로 상품 검색 |
| `createProduct` | 상품 생성 |
| `updateProduct` | 상품 수정 |
| `deleteProduct` | 상품 삭제 |

## 🖥️ Claude Desktop 연동

### 자동 등록
```bash
./mvnw clean package
./register-claude.sh
```

### 수동 등록

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "stdio-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/YOUR_PATH/stdio-mcp-server/target/stdio-mcp-server-1.0.0.jar"
      ],
      "env": {
        "JAVA_HOME": "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
      }
    }
  }
}
```

Claude Desktop을 재시작하면 MCP 서버가 자동으로 연결됩니다.

## 📝 사용 예시

### Claude Desktop에서

```
모든 사용자 정보를 조회해줘
이메일이 john@example.com인 사용자를 찾아줘
이름에 "John"이 포함된 사용자를 검색해줘
이름이 "Jane Doe"이고 이메일이 "jane@example.com"인 사용자를 생성해줘

모든 상품 정보를 조회해줘
"Laptop"이라는 이름의 상품을 검색해줘
```

### REST API로

```bash
# 사용자 생성
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'

# 상품 생성
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"High-end laptop","price":1500.00,"stock":10}'
```

## 🔍 엔드포인트

| URL | 설명 |
|-----|------|
| http://localhost:8082/swagger-ui.html | Swagger UI |
| http://localhost:8082/h2-console | H2 Database Console |
| http://localhost:8082/actuator/health | Health Check |
| http://localhost:8082/actuator/metrics | Metrics |
| http://localhost:8082/api/users | User API |
| http://localhost:8082/api/products | Product API |

## 📚 문서

- [README.md](./README.md) - 상세 문서
- [QUICK_START.md](./QUICK_START.md) - 빠른 시작 가이드

## 🤝 기여

프로젝트에 기여하고 싶으시다면 Pull Request를 보내주세요!

## 📄 라이선스

Apache 2.0

## 📧 문의

- Email: support@example.com
- GitHub Issues: [프로젝트 이슈](https://github.com/YOUR_REPO/issues)

---

**Made with ❤️ using Spring AI & MCP Protocol**
