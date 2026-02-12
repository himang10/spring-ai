# trace-call-app - 분산 트레이싱 테스트 애플리케이션

Spring Boot 기반의 분산 트레이싱 테스트 애플리케이션입니다. 이 애플리케이션은 **trace-called-app**을 호출하여 서비스 간 분산 트레이싱을 테스트합니다.

## 기능

### API Endpoints

- **GET /api/test/health** - 헬스 체크
- **GET /api/test/traces** - 테스트 트레이스 생성
- **GET /api/test/call-service** - trace-called-app 서비스 호출 (분산 트레이싱)
- **GET /api/test/call-database** - 데이터베이스 연동 테스트
- **GET /api/test/full-trace** - 완전한 분산 트레이스 (call → called → DB)
- **GET /api/test/logs?count=10** - 테스트 로그 생성
- **GET /api/test/stress?duration=5** - 스트레스 테스트
- **POST /api/test/simulate-error** - 에러 시뮬레이션

### Observability Features

1. **Distributed Tracing**: OpenTelemetry Java Agent를 통한 자동 분산 트레이싱
2. **Service-to-Service Calls**: trace-called-app과의 HTTP 통신 추적
3. **Metrics**: Micrometer + Prometheus 메트릭
4. **Logs**: 구조화된 로깅 with trace correlation

## OpenTelemetry 설정

이 애플리케이션은 OpenTelemetry Java Agent를 사용하여 자동으로 분산 트레이싱을 수행합니다.

### 환경변수 설정

#### 필수 설정
```yaml
# 트레이스에서 표시될 서비스 이름
OTEL_SERVICE_NAME: "trace-call-app"

# Alloy 수집기 엔드포인트
OTEL_EXPORTER_OTLP_ENDPOINT: "http://grafana-k8s-monitoring-alloy-receiver.observability.svc:4317"

# OTLP 전송 프로토콜 (grpc 또는 http/protobuf)
OTEL_EXPORTER_OTLP_PROTOCOL: "grpc"
```

#### 데이터 타입별 Exporter 설정
```yaml
# 트레이스 데이터를 OTLP로 내보내기
OTEL_TRACES_EXPORTER: "otlp"

# 메트릭 데이터를 OTLP로 내보내기  
OTEL_METRICS_EXPORTER: "otlp"

# 로그 데이터를 OTLP로 내보내기
OTEL_LOGS_EXPORTER: "otlp"
```

#### 서비스 메타데이터 설정
```yaml
# 서비스 메타데이터 속성
OTEL_RESOURCE_ATTRIBUTES: "service.version=1.0.0,deployment.environment=production"
```

**메타데이터 설명:**
이 설정은 모든 트레이스, 메트릭, 로그에 자동으로 포함될 서비스 정보를 정의
- `service.version`: 애플리케이션 버전 정보
- `deployment.environment`: 배포 환경 (dev, staging, production)

#### 트레이스 샘플링 설정
```yaml
# 트레이스 샘플링 전략
OTEL_TRACES_SAMPLER: "always_on"
```

**샘플링 전략:**
1. **`always_on`**: 모든 요청(100%) 추적
   - 장점: 완전한 가시성
   - 단점: 높은 오버헤드
   - 사용: 개발/테스트 환경

2. **`always_off`**: 트레이스 수집 안함(0%)
   - 사용: 디버깅 시 일시 비활성화

3. **`traceidratio`**: 비율 기반 샘플링 (프로덕션 권장)
   ```yaml
   OTEL_TRACES_SAMPLER: "traceidratio"
   OTEL_TRACES_SAMPLER_ARG: "0.1"  # 10% 샘플링
   ```

#### 추가 설정
```yaml
# 메트릭 전송 주기 (밀리초)
OTEL_METRIC_EXPORT_INTERVAL: "30000"

# OpenTelemetry 에이전트 디버그 로그 활성화 여부
OTEL_JAVAAGENT_DEBUG: "false"
```

### 데이터 흐름

```
trace-call-app → Alloy Receiver → [Tempo, Jaeger, Loki, Prometheus]
                                    ↓
                              Grafana Dashboard
```

## 특징

- **Zero-instrumentation Tracing**: Beyla DaemonSet이 애플리케이션 코드 수정 없이 자동으로 HTTP 트레이스 캡처
- **Prometheus Metrics**: Spring Boot Actuator를 통한 애플리케이션 메트릭 노출
- **Structured Logging**: 모든 로그가 Alloy를 통해 Loki로 자동 수집

## 사용법

### 로컬 테스트
```bash
# 현재 K8s 클러스터에서 Alloy 서비스를 로컬로 포워딩
kubectl port-forward -n observability svc/grafana-k8s-monitoring-alloy-receiver 4317:4317

# 로컬에서 실행 시 환경변수 설정
export OTEL_SERVICE_NAME="trace-call-app-local"
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317" 
export OTEL_TRACES_SAMPLER="always_on"
./mvnw spring-boot:run
```

### Kubernetes 배포
```bash
kubectl apply -f k8s/
```

### 테스트 호출
```bash
# Health Check
curl http://trace-call-app.observability.svc/api/test/health

# 분산 트레이싱 테스트 (trace-called-app 호출)
curl http://trace-call-app.observability.svc/api/test/call-service

# 완전한 분산 트레이스 (call → called → DB)
curl http://trace-call-app.observability.svc/api/test/full-trace

# 메트릭 확인  
curl http://trace-call-app.observability.svc/actuator/prometheus

# 로그 생성 테스트
curl http://trace-call-app.observability.svc/api/test/logs?count=10

# 스트레스 테스트 (5초)
curl http://trace-call-app.observability.svc/api/test/stress?duration=5

# 에러 시뮬레이션
curl -X POST http://trace-call-app.observability.svc/api/test/simulate-error
```

### 샘플링 설정 변경 테스트
```bash
# 100% 샘플링으로 배포
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER="always_on"

# 10% 샘플링으로 변경  
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER="traceidratio"
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER_ARG="0.1"

# 샘플링 비활성화
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER="always_off"
```

## 빌드 및 배포

### 1. 애플리케이션 빌드

```bash
./mvnw clean package
```

### 2. Docker 이미지 빌드

```bash
chmod +x docker-build.sh
./docker-build.sh
```

### 3. Docker 이미지 푸시

```bash
chmod +x docker-push.sh
./docker-push.sh
```

### 4. Kubernetes 배포

```bash
kubectl apply -f k8s/
```

## 환경 설정

### OpenTelemetry 설정

이 애플리케이션은 다음 OTLP 엔드포인트로 트레이스를 전송합니다:
- 엔드포인트: `http://grafana-k8s-monitoring-alloy-receiver.observability.svc:4318`

### Prometheus 메트릭

메트릭은 다음 경로에서 확인 가능합니다:
- `/actuator/prometheus`
- `/actuator/metrics`

### 테스트 시나리오

#### 분산 트레이싱 테스트
```bash
# 서비스 간 호출 트레이스 (trace-call-app → trace-called-app)
curl http://trace-call-app.observability.svc/api/test/call-service

# 완전한 분산 트레이스 (call → called → DB)
curl http://trace-call-app.observability.svc/api/test/full-trace

# 데이터베이스 연동 트레이스
curl http://trace-call-app.observability.svc/api/test/call-database
```

#### 샘플링 전략 테스트
```bash
# 1. 모든 요청 추적 (개발/테스트용)
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER="always_on"

# 2. 10% 샘플링 (프로덕션 권장)  
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER="traceidratio"
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER_ARG="0.1"

# 3. 1% 샘플링 (고트래픽 환경)
kubectl set env deployment/trace-call-app OTEL_TRACES_SAMPLER_ARG="0.01"

# 여러 요청 생성 후 Grafana에서 샘플링 비율 확인
for i in {1..100}; do curl http://trace-call-app.observability.svc/api/test/call-service; done
```

#### 로그 상관관계 테스트
```bash
# 트레이스 ID가 포함된 로그 생성
curl http://trace-call-app.observability.svc/api/test/logs?count=10

# Grafana Loki에서 다음 쿼리로 확인:
# {namespace="observability", app="trace-call-app"} |= "traceId"
```

#### 스트레스 및 에러 시뮬레이션
```bash
# 스트레스 테스트 (10초간)
curl http://trace-call-app.observability.svc/api/test/stress?duration=10

# 에러율 30%로 에러 시뮬레이션  
curl -X POST http://trace-call-app.observability.svc/api/test/simulate-error?errorRate=30

# Grafana에서 에러 트레이스 및 메트릭 확인
```

## 모니터링 확인

### Grafana Dashboard
1. Grafana에 접속  
2. Explore 메뉴에서 다음 확인:
   - **Loki**: 로그 데이터
   - **Tempo**: 트레이스 데이터
   - **Jaeger**: 분산 트레이스 시각화 (Cassandra 백엔드)
   - **Prometheus**: 메트릭 데이터

### OpenTelemetry 데이터 흐름 확인

#### 1. Alloy Receiver 상태 확인
```bash
# Alloy 상태 확인
kubectl get pods -n observability -l app.kubernetes.io/name=alloy-receiver

# Alloy 로그 확인 (OTLP 수신 확인)
kubectl logs -n observability -l app.kubernetes.io/name=alloy-receiver -f
```

#### 2. Beyla DaemonSet 상태 확인
```bash
# Beyla DaemonSet 확인
kubectl get pods -n observability -l app=beyla

# Beyla 로그 확인 (eBPF 인스트루멘테이션 확인)
kubectl logs -n observability -l app=beyla -f
```

#### 3. 전체 파이프라인 헬스 체크
```bash
# 파이프라인 헬스 체크 스크립트 실행
./check-pipeline-health.sh
```

### 쿼리 예시

#### Loki (로그)
```
# trace-call-app 로그
{namespace="observability", app="trace-call-app"}

# 트레이스 ID별 로그 검색
{namespace="observability", app="trace-call-app"} |= "traceId"

# 에러 로그만 필터링
{namespace="observability", app="trace-call-app"} |= "ERROR"
```

#### Tempo (트레이스)
```
# 서비스별 트레이스 검색
{service.name="trace-call-app"}

# 분산 트레이스 검색 (서비스 간 호출)
{service.name="trace-call-app"} && {service.name="trace-called-app"}
```

#### Jaeger (트레이스 시각화)
- Service: `trace-call-app` 선택
- Operation: `GET /api/test/call-service` 선택
- 시간 범위 설정 후 "Find Traces" 실행

#### Prometheus (메트릭)
```
# HTTP 요청 메트릭
http_requests_total{job="trace-call-app"}

# JVM 메트릭
jvm_memory_used_bytes{job="trace-call-app"}

# OpenTelemetry 샘플링 비율
otel_sampler_ratio{job="trace-call-app"}

# 분산 트레이스 처리 시간
http_request_duration_seconds{service_name="trace-call-app"}
```

### 트러블슈팅

#### 트레이스가 나타나지 않는 경우
1. **샘플링 설정 확인**:
   ```bash
   kubectl get deployment trace-call-app -o yaml | grep -A5 OTEL_TRACES_SAMPLER
   ```

2. **Alloy 엔드포인트 연결 확인**:
   ```bash
   kubectl get svc -n observability grafana-k8s-monitoring-alloy-receiver
   kubectl exec -it deployment/trace-call-app -- nslookup grafana-k8s-monitoring-alloy-receiver.observability.svc
   ```

3. **OpenTelemetry Java Agent 로그 확인**:
   ```bash
   kubectl set env deployment/trace-call-app OTEL_JAVAAGENT_DEBUG=true
   kubectl logs -f deployment/trace-call-app
   ```

#### 메트릭이 수집되지 않는 경우
1. **Prometheus 엔드포인트 확인**:
   ```bash
   kubectl port-forward deployment/trace-call-app 8080:8080
   curl http://localhost:8080/actuator/prometheus
   ```

2. **Alloy 메트릭 수집 설정 확인**:
   ```bash
   kubectl get configmap -n observability | grep alloy
   ```

## 개발 정보

- **Framework**: Spring Boot 3.4.3
- **Java Version**: 21
- **Observability**: 
  - OpenTelemetry Java Agent (자동 인스트루멘테이션)
  - Micrometer (메트릭)
  - Beyla DaemonSet (eBPF 기반 자동 트레이싱)
- **Namespace**: observability
- **Service Dependencies**: trace-called-app, MariaDB

### OpenTelemetry 구성 요소
1. **Java Agent**: 애플리케이션 레벨 트레이스 컨텍스트 전파
2. **Beyla**: Kubernetes DaemonSet으로 eBPF 기반 자동 HTTP 트레이싱
3. **Alloy**: 중앙 집중화된 OTLP 수집기 (k8sattributes 프로세서로 메타데이터 자동 추가)
4. **Backends**: Jaeger (Cassandra), Tempo, Loki, Prometheus

### 환경변수 참고사항
- `OTEL_RESOURCE_ATTRIBUTES`에 `service.namespace` 설정 불필요 (Alloy가 `k8s.namespace.name` 자동 추가)
- 프로덕션 환경에서는 `OTEL_TRACES_SAMPLER=traceidratio`와 적절한 샘플링 비율 권장
- 개발/테스트에서는 `OTEL_TRACES_SAMPLER=always_on`으로 전체 가시성 확보