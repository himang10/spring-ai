# Langfuse 연동 가이드 (환경변수 방식)

Spring AI 애플리케이션과 Langfuse를 **환경변수만으로** 간단하게 연동합니다.

## 📋 개요

이 프로젝트는 **환경변수**와 **OpenTelemetry**를 사용하여 Langfuse와 연동합니다.
- ✅ **추가 코드 불필요** - application.yml만 있으면 됨
- ✅ **간단한 설정** - 환경변수 2개만 설정
- ✅ **표준 방식** - OpenTelemetry 표준 사용

### 아키텍처

```
Spring AI 애플리케이션
    ↓ (Spring AI Observation)
OpenTelemetry SDK
    ↓ (OTLP Protocol)
Langfuse Server
    ↓
데이터베이스 (PostgreSQL + ClickHouse)
```

## 🚀 1. Langfuse 배포 (Kubernetes)

### 1.1 Helm으로 배포

```bash
# Langfuse Helm 디렉토리로 이동
cd langfuse/helm

# Namespace 생성
kubectl create namespace langfuse

# Helm 배포
helm install langfuse . -n langfuse -f values.yaml

# 배포 확인
kubectl get pods -n langfuse
kubectl get ingress -n langfuse
```

### 1.2 배포 상태 확인

```bash
# 모든 Pod가 Running 상태인지 확인
kubectl get pods -n langfuse

# 예상되는 Pod 목록:
# - langfuse-xxxxx (웹 애플리케이션)
# - langfuse-postgresql-0 (PostgreSQL)
# - langfuse-clickhouse-0 (ClickHouse)
# - langfuse-redis-master-0 (Redis)
# - langfuse-minio-xxxxx (MinIO/S3)
```

### 1.3 Ingress 확인

```bash
kubectl get ingress -n langfuse

# 출력:
# NAME       HOSTS                                      ADDRESS
# langfuse   langfuse.skala25a.project.skala-ai.com    xxx.xxx.xxx.xxx
```

## 🔑 2. Langfuse에서 API 키 발급

### 2.1 웹 브라우저에서 접속

```
https://langfuse.skala25a.project.skala-ai.com
```

### 2.2 첫 계정 생성 (최초 1회만)

1. **"Sign Up"** 버튼 클릭
2. 이메일과 비밀번호 입력
3. 계정 생성
   - ⭐ **첫 번째 가입자는 자동으로 관리자 권한 부여됨**

### 2.3 프로젝트 생성

1. 로그인 후 대시보드에서 **"New Project"** 또는 **"Create Project"** 클릭
2. 프로젝트 이름 입력 (예: `spring-ai-demo`)
3. **Create** 클릭

### 2.4 API 키 발급 ⭐

1. 생성한 프로젝트 선택
2. 좌측 메뉴에서 **Settings** 클릭
3. **API Keys** 탭 선택
4. **"Create new API Key"** 버튼 클릭
5. 다음 정보가 표시됩니다:

```
Public Key:  pk-lf-1234567890abcdef1234567890abcdef
Secret Key:  sk-lf-abcdef1234567890abcdef1234567890
```

⚠️ **중요**: **Secret Key는 단 한 번만 표시**되므로 반드시 안전한 곳에 복사하여 저장하세요!

## 💻 3. 환경변수 설정

Langfuse는 OpenTelemetry를 통해 연동되므로, **OTEL 환경변수**를 설정해야 합니다.

### 3.1 Base64 인증 헤더 생성

```bash
# Public Key와 Secret Key를 콜론(:)으로 연결하여 Base64 인코딩
AUTH=$(echo -n "pk-lf-1234567890abcdef:sk-lf-abcdef1234567890" | base64)
echo $AUTH

# 출력 예:
# cGstbGYtMTIzNDU2Nzg5MGFiY2RlZjpzay1sZi1hYmNkZWYxMjM0NTY3ODkw
```

### 3.2 환경변수 설정

#### macOS/Linux (Bash/Zsh):

```bash
# 1. Base64 인증 문자열 생성
export AUTH=$(echo -n "pk-lf-YOUR_PUBLIC_KEY:sk-lf-YOUR_SECRET_KEY" | base64)

# 2. OpenTelemetry 환경변수 설정
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 3. 기타 필수 환경변수
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-weather-key"

# 확인
echo $OTEL_EXPORTER_OTLP_ENDPOINT
echo $OTEL_EXPORTER_OTLP_HEADERS
```

#### Windows (PowerShell):

```powershell
# 1. Base64 인증 문자열 생성
$AUTH = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("pk-lf-YOUR_PUBLIC_KEY:sk-lf-YOUR_SECRET_KEY"))

# 2. OpenTelemetry 환경변수 설정
$env:OTEL_EXPORTER_OTLP_ENDPOINT="https://langfuse.skala25a.project.skala-ai.com/api/public/otel"
$env:OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 3. 기타 필수 환경변수
$env:OPEN_AI_KEY="sk-xxxxx..."
$env:WEATHER_API_KEY="your-weather-key"
```

### 3.3 영구 설정 (macOS/Linux)

`~/.zshrc` 또는 `~/.bashrc`에 추가:

```bash
# Langfuse OpenTelemetry 설정
export AUTH=$(echo -n "pk-lf-YOUR_PUBLIC_KEY:sk-lf-YOUR_SECRET_KEY" | base64)
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 다른 API 키들
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-weather-key"
```

저장 후:
```bash
source ~/.zshrc  # 또는 source ~/.bashrc
```

## 🏃 4. 애플리케이션 실행

### 4.1 실행 스크립트 사용

```bash
# 실행 권한 부여 (최초 1회)
chmod +x run.sh

# 애플리케이션 실행
./run.sh
```

### 4.2 Maven 직접 실행

```bash
mvn spring-boot:run
```

### 4.3 실행 로그 확인

정상적으로 연동되면 다음과 같은 로그가 출력됩니다:

```
✅ OpenAI API Key 확인 완료
✅ Langfuse (OpenTelemetry) 설정 확인 완료

============================================
Spring AI Tools + Langfuse 데모를 시작합니다.
============================================

... (Spring Boot 시작 로그) ...

Started MyFirstSpringAiApplication in 3.456 seconds
```

## 🧪 5. 테스트

### 5.1 웹 UI 접속

```
http://localhost:8080
```

### 5.2 AI 기능 실행

다음 기능들을 테스트하면 Langfuse에 자동으로 트레이스가 기록됩니다:

1. **날씨 조회**
   - "서울 날씨 알려줘"
   - Weather Tool 호출 추적

2. **날짜/시간**
   - "오늘 날짜 알려줘"
   - DateTime Tool 호출 추적

3. **음악 추천**
   - "신나는 음악 추천해줘"
   - Music Tool 호출 추적

4. **파일시스템**
   - "현재 디렉토리의 파일 목록 보여줘"
   - FileSystem Tool 호출 추적

### 5.3 Langfuse에서 확인

1. Langfuse 웹 UI 접속:
   ```
   https://langfuse.skala25a.project.skala-ai.com
   ```

2. 로그인

3. 프로젝트 선택

4. **"Traces"** 메뉴 클릭

5. 확인 가능한 정보:
   - 📝 **요청/응답 내용** (프롬프트 및 AI 응답)
   - 🔢 **토큰 사용량** (Prompt Tokens, Completion Tokens, Total)
   - ⏱️ **응답 시간** (Latency, Duration)
   - 🤖 **모델 정보** (gpt-4o-mini, text-embedding-3-small 등)
   - 💰 **비용 추적** (예상 비용 계산)
   - 🛠️ **Tool 호출** (Function Calling 추적)

## 📊 6. Langfuse에 기록되는 정보

### 6.1 Trace 구조

```
Trace (전체 요청)
├── Span: Chat Model Request
│   ├── Input: 사용자 프롬프트
│   ├── Output: AI 응답
│   ├── Metadata: 모델, temperature, max_tokens
│   └── Usage: prompt_tokens, completion_tokens
├── Span: Tool Call (Weather API)
│   ├── Input: 도시 이름
│   └── Output: 날씨 정보
└── Span: Embedding (벡터 검색 시)
    ├── Input: 검색 쿼리
    └── Metadata: 임베딩 모델
```

### 6.2 수집되는 데이터

| 항목 | 설명 | 예시 |
|------|------|------|
| **Trace ID** | 전체 요청 고유 ID | `abc123-def456-...` |
| **Input** | 사용자 입력 프롬프트 | "서울 날씨 알려줘" |
| **Output** | AI 생성 응답 | "서울의 현재 날씨는..." |
| **Model** | 사용된 LLM 모델 | gpt-4o-mini |
| **Tokens** | 토큰 사용량 | Prompt: 50, Completion: 120 |
| **Latency** | 응답 시간 | 1.234s |
| **Timestamp** | 요청 시각 | 2026-01-08 22:30:00 |
| **Tags** | 커스텀 태그 | production, weather-tool |

## 🔧 7. 설정 커스터마이징

### 7.1 프롬프트/응답 로깅 비활성화

민감한 정보가 포함된 경우 로깅을 비활성화할 수 있습니다.

[application.yml](src/main/resources/application.yml):
```yaml
spring:
  ai:
    chat:
      observations:
        log-prompt: false      # 프롬프트 로깅 비활성화
        log-completion: false  # 응답 로깅 비활성화
```

### 7.2 샘플링 비율 조정

모든 요청을 추적하지 않고 일부만 샘플링:

```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10%만 샘플링 (프로덕션에서 비용 절감)
```

### 7.3 다른 OTLP Collector 사용

Langfuse 대신 Grafana, Jaeger 등 다른 collector 사용:

```bash
# Grafana Alloy 사용 예시
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318/v1/traces
unset OTEL_EXPORTER_OTLP_HEADERS  # 인증 불필요
```

## 🔍 8. 트러블슈팅

### 8.1 "Langfuse 환경변수가 설정되지 않았습니다" 경고

**증상**: 애플리케이션은 실행되지만 Langfuse에 데이터가 전송되지 않음

**해결**:
```bash
# 환경변수 확인
echo $OTEL_EXPORTER_OTLP_ENDPOINT
echo $OTEL_EXPORTER_OTLP_HEADERS

# 설정되지 않았다면:
export AUTH=$(echo -n "pk-lf-YOUR_KEY:sk-lf-YOUR_KEY" | base64)
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"
```

### 8.2 "401 Unauthorized" 에러

**원인**: API 키가 잘못되었거나 Base64 인코딩이 올바르지 않음

**해결**:
```bash
# 1. API 키 재확인 (Langfuse 웹 UI)
# 2. Base64 인코딩 재생성
AUTH=$(echo -n "CORRECT_PUBLIC_KEY:CORRECT_SECRET_KEY" | base64)
echo $AUTH

# 3. 환경변수 다시 설정
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"
```

### 8.3 Langfuse에 데이터가 보이지 않음

**확인 체크리스트**:

1. **환경변수 설정 확인**:
   ```bash
   env | grep OTEL
   ```

2. **Langfuse 서버 상태 확인**:
   ```bash
   kubectl get pods -n langfuse
   curl -I https://langfuse.skala25a.project.skala-ai.com
   ```

3. **네트워크 연결 확인**:
   ```bash
   curl -X POST https://langfuse.skala25a.project.skala-ai.com/api/public/otel \
     -H "Authorization: Basic $AUTH" \
     -H "Content-Type: application/json" \
     -d '{}'
   ```

4. **애플리케이션 로그 확인**:
   ```bash
   # OpenTelemetry 관련 로그 검색
   mvn spring-boot:run | grep -i "otel\|telemetry\|trace"
   ```

5. **올바른 프로젝트 선택 확인**:
   - Langfuse 웹 UI에서 API 키가 발급된 프로젝트를 선택했는지 확인

### 8.4 Connection refused / Timeout 에러

**원인**: Langfuse 서버에 접근할 수 없음

**해결**:

1. **Ingress 확인**:
   ```bash
   kubectl get ingress -n langfuse
   kubectl describe ingress langfuse -n langfuse
   ```

2. **DNS 확인**:
   ```bash
   nslookup langfuse.skala25a.project.skala-ai.com
   ```

3. **Pod 로그 확인**:
   ```bash
   kubectl logs -n langfuse -l app=langfuse
   ```

### 8.5 Maven 빌드 에러

**증상**: 의존성 다운로드 실패

**해결**:
```bash
# Maven 캐시 정리
mvn clean

# 의존성 강제 업데이트
mvn dependency:resolve -U

# 재빌드
mvn clean install
```

## 🔐 9. 보안 고려사항

### 9.1 API 키 관리

- ✅ **권장**: 환경변수 사용
- ✅ **프로덕션**: Kubernetes Secret 사용
- ❌ **금지**: 코드에 하드코딩
- ❌ **금지**: Git에 커밋

### 9.2 민감 정보 로깅

프롬프트와 응답에 개인정보, 비밀번호 등이 포함될 수 있으므로:

```yaml
spring:
  ai:
    chat:
      observations:
        log-prompt: false      # 민감 정보 포함 시 비활성화
        log-completion: false
```

### 9.3 Kubernetes Secret 사용 (프로덕션)

```bash
# Secret 생성
AUTH=$(echo -n "pk-lf-xxx:sk-lf-xxx" | base64)
kubectl create secret generic langfuse-auth \
  --from-literal=otel-endpoint=https://langfuse.skala25a.project.skala-ai.com/api/public/otel \
  --from-literal=otel-headers="Authorization=Basic $AUTH" \
  -n your-namespace

# Deployment에서 사용
# deployment.yaml:
env:
  - name: OTEL_EXPORTER_OTLP_ENDPOINT
    valueFrom:
      secretKeyRef:
        name: langfuse-auth
        key: otel-endpoint
  - name: OTEL_EXPORTER_OTLP_HEADERS
    valueFrom:
      secretKeyRef:
        name: langfuse-auth
        key: otel-headers
```

## 📚 10. 참고 자료

- [Langfuse 공식 문서](https://langfuse.com/docs)
- [Langfuse + Spring AI 연동 가이드](https://langfuse.com/integrations/frameworks/spring-ai)
- [OpenTelemetry Java 문서](https://opentelemetry.io/docs/languages/java/)
- [Spring AI Observability](https://docs.spring.io/spring-ai/reference/observability/index.html)
- [프로젝트 README](README.md)

## 📧 문의

문제가 발생하면 다음을 확인하세요:
1. 이 문서의 트러블슈팅 섹션
2. Langfuse 공식 문서
3. GitHub Issues: https://github.com/langfuse/langfuse/issues

---

**Sources:**
- [Langfuse + Spring AI Integration](https://langfuse.com/integrations/frameworks/spring-ai)
- [Langfuse Java Client](https://langfuse.com/changelog/2025-03-03-langfuse-java-client)
- [Maven Repository: com.langfuse » langfuse-java](https://mvnrepository.com/artifact/com.langfuse/langfuse-java)
