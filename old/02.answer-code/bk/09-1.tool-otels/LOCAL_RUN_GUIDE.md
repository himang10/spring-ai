# 로컬 PC에서 Spring Boot 실행하여 Loki에 로그 전송하기

## ✅ K8s 배포 없이 로컬에서 가능합니다!

로컬 PC에서 Spring Boot를 실행하면서 OpenTelemetry를 통해 Kubernetes 클러스터의 Loki로 로그를 전송할 수 있습니다.

---

## 📋 전제 조건

1. ✅ Kubernetes 클러스터에 Grafana Alloy가 실행 중
2. ✅ `kubectl port-forward`로 Alloy receiver 접근 가능
3. ✅ OpenTelemetry Spring Boot Starter 의존성 포함

---

## 🚀 실행 방법

### 1단계: Port Forward 확인/실행

현재 port-forward가 실행 중인지 확인:
```bash
ps aux | grep "port-forward.*alloy-receiver" | grep -v grep
```

실행 중이 아니면:
```bash
kubectl port-forward -n observability svc/grafana-k8s-monitoring-alloy-receiver 4317:4317 4318:4318 &
```

### 2단계: 환경변수 설정

`.env.alloy` 파일 확인:
```bash
cat .env.alloy
```

주요 설정:
```bash
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_SERVICE_NAME=spring-ai-tools
OTEL_TRACES_EXPORTER=otlp
OTEL_LOGS_EXPORTER=otlp
OTEL_METRICS_EXPORTER=otlp
```

### 3단계: Spring Boot 실행

```bash
# .env.alloy 환경변수 로드
export $(cat .env.alloy | grep -v '^#' | xargs)

# 필수 API 키 설정
export OPEN_AI_KEY=your-openai-api-key
export WEATHER_API_KEY=your-weather-api-key

# 애플리케이션 실행
./run-with-alloy.sh
# 또는
mvn spring-boot:run
```

---

## 🔍 Grafana에서 로그 확인

### Loki 쿼리 예시:

```logql
# 1. 서비스 이름으로 검색
{service_name="spring-ai-tools"}

# 2. 네임스페이스 포함 (로컬에서는 없을 수 있음)
{service_name="spring-ai-tools", namespace="skala-practice"}

# 3. 로그 레벨별 필터링
{service_name="spring-ai-tools"} |= "ERROR"
{service_name="spring-ai-tools"} |= "DEBUG"

# 4. 특정 시간대 검색
{service_name="spring-ai-tools"} |= "OpenTelemetry"

# 5. Job 이름으로 검색 (OTLP receiver)
{job="integrations/otlp"}
```

### Grafana Explore 접속:
1. Grafana 접속
2. **Explore** 메뉴 선택
3. Data source: **Loki** 선택
4. 위 쿼리 입력 후 검색

---

## 🔧 트러블슈팅

### 문제 1: Connection refused 에러

```
Failed to connect to localhost:4317
```

**원인:** Port-forward가 실행되지 않음

**해결:**
```bash
# Port-forward 다시 실행
kubectl port-forward -n observability svc/grafana-k8s-monitoring-alloy-receiver 4317:4317 4318:4318 &
```

### 문제 2: Grafana에서 로그가 보이지 않음

**확인 사항:**
1. Spring Boot 애플리케이션이 실행 중인가?
```bash
curl http://localhost:8080/actuator/health
```

2. OTEL 환경변수가 설정되었는가?
```bash
env | grep OTEL
```

3. Alloy가 로그를 받고 있는가?
```bash
kubectl logs -n observability deployment/grafana-k8s-monitoring-alloy-receiver -c alloy --tail=50
```

4. Loki API로 직접 확인:
```bash
# Port-forward Loki
kubectl port-forward -n observability svc/loki 3100:3100 &

# 라벨 확인
curl -s "http://localhost:3100/loki/api/v1/labels" | jq

# service_name 값 확인
curl -s "http://localhost:3100/loki/api/v1/label/service_name/values" | jq

# 최근 로그 쿼리
curl -s 'http://localhost:3100/loki/api/v1/query_range?query={service_name="spring-ai-tools"}&limit=10' | jq
```

### 문제 3: 로그가 몇 초 후에야 나타남

**정상입니다!**
- OpenTelemetry는 배치 방식으로 로그를 전송합니다
- 기본 배치 주기: 5-10초
- 실시간이 아닌 near-realtime입니다

---

## 📊 데이터 흐름

```
로컬 PC Spring Boot
  ↓ (OTLP/gRPC)
localhost:4317 (port-forward)
  ↓
Kubernetes Alloy Receiver (observability ns)
  ↓
Loki (observability ns)
  ↓
Grafana Dashboard
```

---

## 🎯 K8s 배포 vs 로컬 실행

### 로컬 실행 장점:
- ✅ 빠른 개발/테스트 사이클
- ✅ 디버깅 용이
- ✅ 즉시 코드 변경 반영

### 로컬 실행 단점:
- ⚠️ Port-forward 의존성 (네트워크 불안정 시 문제)
- ⚠️ Kubernetes 레이블 (pod, namespace 등) 자동 추가 안 됨
- ⚠️ 실제 프로덕션 환경과 차이

### K8s 배포 장점:
- ✅ 프로덕션과 동일한 환경
- ✅ Kubernetes 메타데이터 자동 추가
- ✅ 네트워크 안정성

---

## 💡 권장 사항

### 개발 단계:
```bash
# 로컬에서 실행
export $(cat .env.alloy | xargs)
./run-with-alloy.sh
```

### 테스트/검증 단계:
```bash
# K8s에 배포
./docker-build.sh
./docker-push.sh
kubectl apply -f k8s/
```

---

## 📝 참고

- OpenTelemetry는 **자동으로 배치 전송**합니다
- **모든 신호**(traces, logs, metrics)가 하나의 엔드포인트로 전송됩니다
- Alloy가 **자동으로 라우팅**하여 Loki(로그), Tempo(트레이스), Mimir(메트릭)로 분배합니다
