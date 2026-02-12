# Grafana Alloy 연동 가이드

Spring AI 애플리케이션의 Trace와 Log를 Grafana Alloy (OTEL Collector)로 전송하는 가이드입니다.

## 📋 개요

**Grafana Alloy**는 OpenTelemetry 프로토콜을 지원하는 텔레메트리 데이터 수집기입니다.
이 가이드는 로컬에서 실행하는 Spring AI 애플리케이션을 Kubernetes에 배포된 Alloy로 연결하는 방법을 설명합니다.

### 아키텍처

```
Spring AI 애플리케이션 (로컬)
    ↓ OTLP (HTTP/Protobuf)
    ↓ https://alloy.skala25a.project.skala-ai.com
Grafana Alloy (Kubernetes)
    ↓
Grafana Loki (Logs)
Grafana Tempo (Traces)
    ↓
Grafana Dashboard
```

## 🚀 빠른 시작 (3단계)

### 1단계: Alloy 연결 테스트

```bash
./test-alloy-connection.sh
```

**예상 결과**:
- DNS 해석 성공
- HTTPS 연결 성공
- OTLP 엔드포인트 응답 (200, 401, 또는 405)

### 2단계: 환경변수 설정

#### 방법 A) 스크립트 사용 (간단)

```bash
# API 키만 설정
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-weather-key"  # 선택사항

# 실행 (Alloy 환경변수 자동 설정)
./run-with-alloy.sh
```

#### 방법 B) .env 파일 사용

```bash
# 1. 템플릿 복사
cp .env.alloy.example .env.alloy

# 2. .env.alloy 파일 수정
vim .env.alloy

# 3. 환경변수 로드
export $(cat .env.alloy | xargs)

# 4. 실행
mvn spring-boot:run
```

### 3단계: 실행 및 확인

```bash
./run-with-alloy.sh
```

**로그 확인**:
```
✅ OpenAI API Key 확인 완료

============================================
OpenTelemetry 설정
============================================
OTLP Collector: Grafana Alloy
Endpoint: https://alloy.skala25a.project.skala-ai.com

설정된 환경변수:
  OTEL_EXPORTER_OTLP_ENDPOINT=https://alloy.skala25a.project.skala-ai.com
  OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
  ...
```

## 🔧 환경변수 설명

### 필수 환경변수

| 환경변수 | 설명 | 예시 |
|---------|------|------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Alloy OTLP 엔드포인트 | `https://alloy.skala25a.project.skala-ai.com` |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | OTLP 프로토콜 | `http/protobuf` (기본값) |

### 선택적 환경변수

| 환경변수 | 설명 | 기본값 |
|---------|------|--------|
| `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` | Traces 전용 엔드포인트 | `${ENDPOINT}/v1/traces` |
| `OTEL_EXPORTER_OTLP_LOGS_ENDPOINT` | Logs 전용 엔드포인트 | `${ENDPOINT}/v1/logs` |
| `OTEL_SERVICE_NAME` | 서비스 이름 | `spring-ai-tools` |
| `OTEL_RESOURCE_ATTRIBUTES` | Resource 속성 | `service.name=...` |
| `OTEL_EXPORTER_OTLP_HEADERS` | 인증 헤더 (필요 시) | `Authorization=Bearer ...` |

## 📡 OTLP 프로토콜

Alloy는 다음 프로토콜을 지원합니다:

### 1. HTTP/Protobuf (추천)

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT=https://alloy.skala25a.project.skala-ai.com
export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
```

**엔드포인트**:
- Traces: `https://alloy.skala25a.project.skala-ai.com/v1/traces`
- Logs: `https://alloy.skala25a.project.skala-ai.com/v1/logs`
- Metrics: `https://alloy.skala25a.project.skala-ai.com/v1/metrics`

### 2. gRPC

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT=https://alloy.skala25a.project.skala-ai.com
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
```

⚠️ **주의**: Ingress가 gRPC를 지원하는지 확인 필요

## 🔍 전송되는 데이터

### Traces

```
Trace
├── Span: Chat Model Request
│   ├── Attributes:
│   │   ├── gen_ai.request.model: gpt-4o-mini
│   │   ├── gen_ai.request.temperature: 0.7
│   │   └── gen_ai.request.max_tokens: 2000
│   ├── Events:
│   │   ├── Prompt
│   │   └── Completion
│   └── Duration: 1.234s
└── Span: Tool Execution (Weather API)
    ├── Attributes:
    │   └── tool.name: weather
    └── Duration: 0.456s
```

### Logs

```
LogRecord
├── Timestamp
├── Severity: INFO, WARN, ERROR
├── Body: Log message
└── Attributes:
    ├── service.name: spring-ai-tools
    ├── logger.name: com.example.springai
    └── thread.name: http-nio-8080-exec-1
```

## 🧪 테스트

### 1. Alloy 연결 테스트

```bash
./test-alloy-connection.sh
```

### 2. 애플리케이션 실행

```bash
./run-with-alloy.sh
```

### 3. AI 기능 테스트

브라우저: http://localhost:8080

테스트 프롬프트:
- "서울 날씨 알려줘" (Weather Tool 호출)
- "오늘 날짜 알려줘" (DateTime Tool 호출)
- "신나는 음악 추천해줘" (Music Tool 호출)

### 4. Grafana에서 확인

Grafana 대시보드에서 확인:
- **Tempo**: Traces 검색
  - Service: `spring-ai-tools`
  - Operation: `ChatClient.call`
- **Loki**: Logs 검색
  - Label: `{service_name="spring-ai-tools"}`

## 🐛 트러블슈팅

### 1. "Connection refused" 에러

**원인**: Alloy 엔드포인트에 연결할 수 없음

**해결**:
```bash
# DNS 확인
nslookup alloy.skala25a.project.skala-ai.com

# 연결 테스트
curl -I https://alloy.skala25a.project.skala-ai.com

# Alloy Pod 상태 확인
kubectl get pods -n monitoring -l app=alloy
```

### 2. "401 Unauthorized" 에러

**원인**: Alloy에 인증이 필요함

**해결**:
```bash
# 인증 헤더 추가
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer YOUR_TOKEN"
```

### 3. Trace가 보이지 않음

**체크리스트**:
- [ ] Alloy가 실행 중인가?
- [ ] 환경변수가 올바르게 설정되었나?
- [ ] application.yml에서 `management.tracing.sampling.probability`가 1.0인가?
- [ ] Alloy 설정에서 OTLP receiver가 활성화되었나?

**확인 명령**:
```bash
# 환경변수 확인
env | grep OTEL

# Alloy 로그 확인
kubectl logs -n monitoring -l app=alloy
```

### 4. "404 Not Found" 에러

**원인**: OTLP 엔드포인트 경로가 잘못됨

**해결**:
```bash
# 경로 확인
# HTTP: /v1/traces, /v1/logs
# 또는 Alloy 설정에 따라 /otlp/v1/traces 등 다를 수 있음

# Alloy Ingress 확인
kubectl get ingress -n monitoring alloy -o yaml
```

### 5. SSL/TLS 에러

**원인**: HTTPS 인증서 문제

**해결** (개발 환경 only):
```bash
# SSL 검증 비활성화 (권장하지 않음)
export OTEL_EXPORTER_OTLP_INSECURE=true
```

## 🔐 보안 고려사항

### 1. 프로덕션 환경

```bash
# Bearer Token 사용
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer YOUR_SECURE_TOKEN"

# 또는 Basic Auth
AUTH=$(echo -n "username:password" | base64)
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"
```

### 2. 민감 정보 로깅 비활성화

```yaml
# application.yml
spring:
  ai:
    chat:
      observations:
        log-prompt: false      # 프롬프트 로깅 비활성화
        log-completion: false  # 응답 로깅 비활성화
```

## 📊 Alloy 설정 예시

Alloy가 OTLP를 받으려면 다음과 같이 설정되어 있어야 합니다:

```hcl
// Alloy 설정 (alloy-config.alloy)

otelcol.receiver.otlp "default" {
  grpc {
    endpoint = "0.0.0.0:4317"
  }

  http {
    endpoint = "0.0.0.0:4318"
  }

  output {
    traces  = [otelcol.exporter.otlp.tempo.input]
    logs    = [otelcol.exporter.loki.default.receiver]
  }
}

otelcol.exporter.otlp "tempo" {
  client {
    endpoint = "tempo:4317"
  }
}

otelcol.exporter.loki "default" {
  forward_to = [loki.write.default.receiver]
}
```

## 🎯 환경변수 전체 예시

```bash
# .env.alloy 파일

# 필수
OPEN_AI_KEY=sk-xxxxx...
WEATHER_API_KEY=your-weather-key

# Alloy OTLP
OTEL_EXPORTER_OTLP_ENDPOINT=https://alloy.skala25a.project.skala-ai.com
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://alloy.skala25a.project.skala-ai.com/v1/traces
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://alloy.skala25a.project.skala-ai.com/v1/logs
OTEL_SERVICE_NAME=spring-ai-tools
OTEL_RESOURCE_ATTRIBUTES=service.name=spring-ai-tools,deployment.environment=local

# 인증 (필요한 경우)
# OTEL_EXPORTER_OTLP_HEADERS=Authorization=Bearer YOUR_TOKEN
```

## 📚 참고 자료

- [Grafana Alloy 공식 문서](https://grafana.com/docs/alloy/latest/)
- [OpenTelemetry 환경변수](https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/)
- [OTLP Specification](https://opentelemetry.io/docs/specs/otlp/)
- [Spring Boot Observability](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.observability)

## 💡 요약

```bash
# 1. 연결 테스트
./test-alloy-connection.sh

# 2. 실행
./run-with-alloy.sh

# 3. Grafana에서 확인
# Tempo: Traces
# Loki: Logs
```

**끝!** Spring AI의 모든 Trace와 Log가 Grafana Alloy를 통해 수집됩니다.
