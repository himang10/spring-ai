# Trace Call App with OpenTelemetry Java Agent

이 애플리케이션은 OpenTelemetry Java Agent를 사용하여 자동 계측(Auto-instrumentation)을 구현합니다.
Java 코드 변경 없이 traces, metrics, logs를 수집하여 Grafana Alloy로 전송하며,
downstream 서비스(trace-called-app)를 호출하여 분산 트레이싱을 구현합니다.

## 주요 특징

- **Zero-Code Instrumentation**: Java 코드 수정 없이 자동 계측
- **OpenTelemetry Java Agent**: 버전 2.11.0 사용
- **OTLP Protocol**: gRPC를 통해 Alloy로 데이터 전송
- **Full Observability**: Traces, Metrics, Logs 모두 수집

## 구성 요소

### 1. Maven 설정 (pom.xml)
- OpenTelemetry Java Agent를 빌드 시 자동 다운로드
- `download-maven-plugin`을 사용하여 agent JAR 파일 획득

### 2. Dockerfile
```dockerfile
# OpenTelemetry Java Agent 포함
COPY target/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# Agent와 함께 애플리케이션 실행
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
```

### 3. Kubernetes 환경 변수
```yaml
# 서비스 식별
- name: OTEL_SERVICE_NAME
  value: "trace-call-app"

# OTLP Exporter 설정
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: "http://grafana-k8s-monitoring-alloy-receiver.observability.svc:4317"
- name: OTEL_EXPORTER_OTLP_PROTOCOL
  value: "grpc"

# Exporters 활성화
- name: OTEL_TRACES_EXPORTER
  value: "otlp"
- name: OTEL_METRICS_EXPORTER
  value: "otlp"
- name: OTEL_LOGS_EXPORTER
  value: "otlp"

# Resource Attributes
- name: OTEL_RESOURCE_ATTRIBUTES
  value: "service.namespace=skala-practice,service.version=1.0.0,deployment.environment=production"

# Sampling (100% for testing)
- name: OTEL_TRACES_SAMPLER
  value: "always_on"
```

## 빌드 및 배포

### 1. Maven 빌드
```bash
cd 06.trace-call-app
./mvnw clean package
```

Maven 빌드 중에 자동으로:
- 애플리케이션 JAR 생성
- OpenTelemetry Java Agent JAR 다운로드 (target/ 디렉토리)

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
# Deployment 배포
kubectl apply -f k8s/deploy.yaml

# Service 생성
kubectl apply -f k8s/service.yaml

# Ingress 생성 (선택사항)
kubectl apply -f k8s/ingress.yaml
```

## 검증

### 1. Pod 상태 확인
```bash
kubectl get pods -n skala-practice -l app=trace-call-app
```

### 2. 로그에서 OpenTelemetry 초기화 확인
```bash
kubectl logs -n skala-practice -l app=trace-call-app | grep -i otel
```

예상 출력:
```
[otel.javaagent] - opentelemetry-javaagent - version: 2.11.0
[otel.javaagent] - Instrumentation enabled for: ...
```

### 3. 테스트 요청 보내기
```bash
# Service를 통한 호출
kubectl run -n skala-practice curl-test --image=curlimages/curl:latest --rm -it --restart=Never -- \
  curl -s http://trace-call-app:8080/api/test/all

# 또는 Ingress를 통한 호출
curl https://trace-call-app.skala25a.project.skala-ai.com/api/test/all
```

### 4. Grafana에서 확인
```bash
# Grafana 접속
https://observability.skala25a.project.skala-ai.com

# Tempo에서 Traces 확인
- Explore > Tempo
- Service: trace-call-app
- Operation: GET /api/test/all

# Prometheus에서 Metrics 확인
- Explore > Prometheus
- Metrics: http_server_request_duration_seconds

# Loki에서 Logs 확인
- Explore > Loki
- Labels: {service_name="trace-call-app"}
```

## OpenTelemetry Java Agent 기능

### 자동 계측되는 라이브러리
- **HTTP Clients**: RestTemplate, WebClient, HttpClient
- **HTTP Servers**: Spring MVC, Spring WebFlux, Servlet
- **Database**: JDBC, Hibernate, JPA
- **Messaging**: Kafka, RabbitMQ, JMS
- **Async**: CompletableFuture, Reactor, RxJava
- **Logging**: Logback, Log4j2, JUL

### 수집되는 데이터
- **Traces**: 
  - HTTP requests/responses
  - Database queries
  - Method spans
  - Async operations
  
- **Metrics**:
  - HTTP request duration
  - HTTP request count
  - JVM metrics
  - System metrics
  
- **Logs**:
  - Application logs with trace context
  - Automatic correlation with traces

## 환경 변수 상세

| 환경 변수 | 설명 | 기본값 |
|----------|------|--------|
| `OTEL_SERVICE_NAME` | 서비스 이름 (필수) | - |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OTLP endpoint (gRPC 또는 HTTP) | - |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | 프로토콜 (grpc 또는 http/protobuf) | grpc |
| `OTEL_TRACES_EXPORTER` | Trace exporter 타입 | otlp |
| `OTEL_METRICS_EXPORTER` | Metrics exporter 타입 | otlp |
| `OTEL_LOGS_EXPORTER` | Logs exporter 타입 | otlp |
| `OTEL_TRACES_SAMPLER` | Sampling 전략 | parentbased_always_on |
| `OTEL_METRIC_EXPORT_INTERVAL` | Metric 전송 간격 (ms) | 60000 |
| `OTEL_RESOURCE_ATTRIBUTES` | Resource attributes | - |
| `OTEL_JAVAAGENT_DEBUG` | Debug 모드 활성화 | false |

## Beyla vs OpenTelemetry Java Agent 비교

| 특징 | Beyla (eBPF) | OTel Java Agent |
|------|--------------|-----------------|
| **설치** | DaemonSet (노드당) | Container 내 Agent |
| **언어 지원** | 언어 무관 | Java만 |
| **코드 변경** | 불필요 | 불필요 |
| **상세도** | 네트워크 레벨 | 애플리케이션 레벨 |
| **메서드 추적** | 불가능 | 가능 |
| **데이터베이스 쿼리** | 불가능 | 가능 |
| **커스텀 속성** | 제한적 | 풍부 |
| **성능 오버헤드** | 매우 낮음 | 낮음 |
| **권한 요구** | Privileged | 없음 |

## 트러블슈팅

### Agent가 로드되지 않는 경우
```bash
# Dockerfile에 agent가 포함되었는지 확인
docker run --rm --entrypoint ls \
  amdp-registry.skala-ai.com/skala25a/trace-call-app:1.0.0 \
  -la /app/

# 예상 출력:
# opentelemetry-javaagent.jar
# app.jar
```

### Traces가 보이지 않는 경우
```bash
# 1. Pod 로그에서 OTLP 연결 확인
kubectl logs -n skala-practice -l app=trace-call-app | grep -i "otlp\|export"

# 2. Alloy receiver가 데이터를 받는지 확인
kubectl logs -n observability -l app.kubernetes.io/component=alloy-receiver | grep -i trace

# 3. Endpoint 연결 테스트
kubectl run -n skala-practice netshoot --image=nicolaka/netshoot --rm -it --restart=Never -- \
  curl -v telnet://grafana-k8s-monitoring-alloy-receiver.observability.svc:4317
```

### Debug 모드 활성화
```yaml
env:
- name: OTEL_JAVAAGENT_DEBUG
  value: "true"
- name: OTEL_JAVAAGENT_LOGGING
  value: "application"
```

## 참고 자료

- [OpenTelemetry Java Instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
- [OpenTelemetry Java Agent Configuration](https://opentelemetry.io/docs/zero-code/java/agent/configuration/)
- [Supported Libraries and Frameworks](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md)
