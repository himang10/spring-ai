# Config 파일 없이 Langfuse 사용하기

Config 파일을 사용하지 않고 **환경변수만으로** Langfuse를 연동하는 방법입니다.

## 방법: 환경변수 직접 설정

### 1단계: API 키 Base64 인코딩

```bash
# Langfuse에서 발급받은 키
PUBLIC_KEY="pk-lf-1234567890abcdef..."
SECRET_KEY="sk-lf-abcdef1234567890..."

# Base64 인코딩
AUTH=$(echo -n "$PUBLIC_KEY:$SECRET_KEY" | base64)
echo $AUTH
# 출력: cGstbGYtMTIzNDU2Nzg5MGFiY2RlZjpzay1sZi1hYmNkZWYxMjM0NTY3ODkw
```

### 2단계: OpenTelemetry 환경변수 설정

```bash
# OpenTelemetry 환경변수 (이것만 있으면 됨!)
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 기타 필수 환경변수
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-key"
```

### 3단계: 실행

```bash
mvn spring-boot:run
```

## Config 파일 제거 (선택사항)

Config 파일을 사용하지 않으려면 삭제해도 됩니다:

```bash
rm src/main/java/com/example/springai/config/LangfuseProperties.java
rm src/main/java/com/example/springai/config/LangfuseAutoConfiguration.java
```

그리고 application.yml에서 langfuse 섹션도 삭제:

```yaml
# application.yml에서 제거
# langfuse:
#   enabled: ...
#   public-key: ...
#   secret-key: ...
```

## 장단점 비교

### Config 파일 사용 (현재 방식)

**장점**:
- ✅ application.yml에서 API 키 관리 가능
- ✅ 환경변수 설정 불필요
- ✅ Base64 인코딩 자동 처리
- ✅ 프로파일별 설정 가능 (dev, prod)

**단점**:
- ❌ Config 파일 2개 추가 필요

### 환경변수만 사용 (Config 없음)

**장점**:
- ✅ Config 파일 불필요
- ✅ 코드 간단

**단점**:
- ❌ Base64 인코딩을 직접 해야 함
- ❌ 환경변수를 매번 설정해야 함
- ❌ 개발 환경마다 별도 설정 필요

## 권장 사항

| 상황 | 권장 방법 |
|------|---------|
| 로컬 개발 (간편함 우선) | Config 파일 사용 (application.yml 설정) |
| 프로덕션 (보안 우선) | 환경변수 사용 (Kubernetes Secret) |
| CI/CD | 환경변수 사용 |
| 팀 협업 | Config 파일 사용 (코드로 관리) |

## 완전히 Config 없이 사용하기

### application.yml 최소 설정

```yaml
spring:
  application:
    name: spring-ai-demo
  ai:
    openai:
      api-key: ${OPEN_AI_KEY}
    chat:
      observations:
        log-prompt: true
        log-completion: true

management:
  tracing:
    sampling:
      probability: 1.0

# Langfuse 관련 설정 없음!
```

### 환경변수 스크립트

```bash
#!/bin/bash
# setup-langfuse.sh

# Langfuse API 키
PUBLIC_KEY="pk-lf-your-key"
SECRET_KEY="sk-lf-your-secret"

# Base64 인코딩
AUTH=$(echo -n "$PUBLIC_KEY:$SECRET_KEY" | base64)

# OpenTelemetry 환경변수
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 기타 필수 환경변수
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-key"

echo "✅ Langfuse 환경변수 설정 완료"
```

### 사용

```bash
# 환경변수 로드
source setup-langfuse.sh

# 애플리케이션 실행
mvn spring-boot:run
```

## 결론

**Config 파일은 편의성을 위한 것**입니다.

- **환경변수만 사용해도 완전히 작동합니다**
- **Config 파일이 하는 일**: application.yml → OpenTelemetry 환경변수 변환
- **선택은 사용자의 필요에 따라**:
  - 간편함 우선 → Config 파일 사용
  - 최소 의존성 우선 → 환경변수만 사용
