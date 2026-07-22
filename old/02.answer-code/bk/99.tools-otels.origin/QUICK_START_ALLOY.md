# Alloy 빠른 시작 가이드

Spring AI를 Grafana Alloy와 연동하는 가장 빠른 방법입니다.

## 🚨 현재 상태

**Alloy 연결 테스트 결과**: 503 Service Temporarily Unavailable

이는 다음 중 하나일 수 있습니다:
- Alloy Pod가 실행되지 않음
- Alloy Pod가 준비되지 않음 (Readiness Probe 실패)
- Alloy Service에 연결된 Endpoint가 없음

## 📋 해결 방법 (2단계)

### 1단계: Alloy 상태 확인

```bash
./check-alloy.sh
```

이 스크립트가 자동으로:
- Alloy Pod 검색
- Pod 상태 확인
- Service 확인
- Ingress 확인
- 로그 확인
- 문제 해결 방법 안내

### 2단계: Alloy 문제 해결

#### Option A) Alloy Pod 재시작

```bash
# Namespace 확인 (monitoring, observability 등)
kubectl get pods -A | grep alloy

# Pod 재시작
kubectl rollout restart deployment -n <namespace> -l app.kubernetes.io/name=alloy
```

#### Option B) Alloy Pod 수동 확인

```bash
# Pod 상태
kubectl get pods -n <namespace> -l app.kubernetes.io/name=alloy

# Pod 이벤트
kubectl describe pod -n <namespace> <alloy-pod-name>

# Pod 로그
kubectl logs -n <namespace> <alloy-pod-name>
```

#### Option C) Service Endpoints 확인

```bash
# Endpoints 확인
kubectl get endpoints -n <namespace> | grep alloy

# 출력이 비어있으면 Pod가 Service에 연결되지 않음
```

## 🚀 Alloy가 준비된 후 실행

### 환경변수 설정 및 실행

```bash
# 1. API 키 설정
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-weather-key"  # 선택

# 2. 애플리케이션 실행 (Alloy 환경변수 자동 설정)
./run-with-alloy.sh
```

### 실행 로그 확인

정상적으로 설정되면 다음과 같이 출력됩니다:

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
  OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://alloy.skala25a.project.skala-ai.com/v1/traces
  OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://alloy.skala25a.project.skala-ai.com/v1/logs
  OTEL_SERVICE_NAME=spring-ai-tools

============================================
애플리케이션 시작
============================================

[INFO] Scanning for projects...
...
Started MyFirstSpringAiApplication in 3.456 seconds
```

## 🧪 테스트

### 1. 웹 UI 접속

```
http://localhost:8080
```

### 2. AI 기능 테스트

프롬프트 입력:
- "서울 날씨 알려줘"
- "오늘 날짜 알려줘"
- "신나는 음악 추천해줘"

### 3. Grafana에서 확인

**Tempo (Traces)**:
- Service: `spring-ai-tools`
- Operation: `ChatClient.call`

**Loki (Logs)**:
- Label: `{service_name="spring-ai-tools"}`

## 🔧 Alloy가 준비되지 않은 경우 대안

### Option 1) 로컬 Alloy 실행

```bash
# Docker로 Alloy 실행
docker run -d \
  --name alloy \
  -p 4317:4317 \
  -p 4318:4318 \
  -v $(pwd)/alloy-config.yaml:/etc/alloy/config.yaml \
  grafana/alloy:latest \
  run --server.http.listen-addr=0.0.0.0:12345 /etc/alloy/config.yaml

# 환경변수 변경
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
```

### Option 2) Langfuse 사용

```bash
# Langfuse로 전환
./run.sh  # 또는 setup-langfuse.sh 사용
```

### Option 3) OTLP 비활성화

```bash
# 환경변수 제거
unset OTEL_EXPORTER_OTLP_ENDPOINT

# 일반 실행
mvn spring-boot:run
```

## 📚 관련 문서

| 문서 | 설명 |
|------|------|
| [ALLOY_SETUP.md](ALLOY_SETUP.md) | 전체 Alloy 연동 가이드 |
| [test-alloy-connection.sh](test-alloy-connection.sh) | 연결 테스트 스크립트 |
| [check-alloy.sh](check-alloy.sh) | Alloy 상태 확인 스크립트 |
| [run-with-alloy.sh](run-with-alloy.sh) | 실행 스크립트 |

## 💡 체크리스트

Alloy 연동 전 확인사항:

- [ ] Alloy Pod가 Running 상태인가?
  ```bash
  kubectl get pods -A | grep alloy
  ```

- [ ] Alloy Service에 Endpoints가 있는가?
  ```bash
  kubectl get endpoints -A | grep alloy
  ```

- [ ] Alloy Ingress가 올바르게 설정되었는가?
  ```bash
  kubectl get ingress -A | grep alloy
  ```

- [ ] Alloy가 OTLP를 받도록 설정되었는가?
  ```bash
  kubectl logs -n <namespace> <alloy-pod> | grep -i otlp
  ```

## 🎯 요약

```bash
# 1. Alloy 상태 확인
./check-alloy.sh

# 2. Alloy 문제 해결
kubectl rollout restart deployment -n <namespace> -l app.kubernetes.io/name=alloy

# 3. 애플리케이션 실행
export OPEN_AI_KEY="sk-xxxxx..."
./run-with-alloy.sh

# 4. Grafana에서 확인
# Tempo: Traces
# Loki: Logs
```

---

**문제가 해결되지 않으면**: [ALLOY_SETUP.md](ALLOY_SETUP.md)의 트러블슈팅 섹션 참고
