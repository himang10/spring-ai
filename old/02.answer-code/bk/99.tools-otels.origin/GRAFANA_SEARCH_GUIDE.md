# Grafana에서 OTEL 로그/Trace 검색하기

## ✅ 확인 완료 사항

1. **Spring Boot 애플리케이션**: 정상 실행 중 ✅
2. **OTLP Exporter**: HTTP/Protobuf (포트 4318) 사용 ✅
3. **Alloy Receiver**: 데이터 수신 중 ✅
   - Logs 수신 확인
   - Metrics 수신 확인
   - Traces 수신 확인

---

## 📊 Grafana에서 데이터 검색하기

### 1️⃣ Grafana 접속

```bash
# Grafana URL 확인 (Ingress 또는 LoadBalancer 사용 시)
kubectl get ingress -n observability

# 또는 port-forward로 로컬 접속
kubectl port-forward -n observability svc/grafana 3000:80
```

브라우저에서 접속:
- Port-forward: http://localhost:3000
- Ingress: https://your-grafana-domain.com

---

### 2️⃣ Tempo에서 Traces 검색

1. **Grafana 좌측 메뉴** → **Explore** 클릭
2. **데이터 소스 선택**: `Tempo` 선택
3. **검색 방법**:

#### Option A: Query 방식
```
{service.name="spring-ai-chat-memory"}
```

#### Option B: Search 탭 사용
- **Service Name**: `spring-ai-chat-memory`
- **Time Range**: Last 15 minutes
- **Run Query** 클릭

#### 검색 필터 예시:
```
# HTTP 요청만 검색
{service.name="spring-ai-chat-memory" && http.method="GET"}

# 특정 URL 경로
{service.name="spring-ai-chat-memory" && http.target="/weather"}

# 에러가 발생한 trace만
{service.name="spring-ai-chat-memory" && status.code=2}
```

---

### 3️⃣ Loki에서 Logs 검색

1. **Grafana 좌측 메뉴** → **Explore** 클릭
2. **데이터 소스 선택**: `Loki` 선택
3. **LogQL 쿼리 입력**:

```logql
# 서비스 이름으로 검색 (실제 서비스 이름은 spring-ai-chat-memory)
{service_name="spring-ai-chat-memory"}

# 특정 레벨 로그만
{service_name="spring-ai-chat-memory"} |= "ERROR"

# 특정 메시지 포함
{service_name="spring-ai-chat-memory"} |= "weather"

# JSON 파싱 후 필터링
{service_name="spring-ai-chat-memory"} | json | level="ERROR"
```

> **⚠️ 중요**: 실제 서비스 이름은 `application.yml`의 `spring.application.name` 값인 `spring-ai-chat-memory`입니다.
> `.env.alloy`의 `OTEL_SERVICE_NAME=spring-ai-tools`는 무시됩니다.

---

### 4️⃣ Prometheus/Mimir에서 Metrics 검색

1. **Grafana 좌측 메뉴** → **Explore** 클릭
2. **데이터 소스 선택**: `Prometheus` 또는 `Mimir` 선택
3. **PromQL 쿼리 입력**:

```promql
# HTTP 요청 수
http_server_requests_seconds_count{service_name="spring-ai-chat-memory"}

# 응답 시간 (95th percentile)
histogram_quantile(0.95, 
  rate(http_server_requests_seconds_bucket{service_name="spring-ai-chat-memory"}[5m])
)

# JVM 메모리 사용량
jvm_memory_used_bytes{service_name="spring-ai-chat-memory"}
```

---

## 🔍 실시간 모니터링

### Trace 확인
```bash
# 1. 요청 전송
curl http://localhost:8080/weather?city=Seoul

# 2. Grafana Tempo에서:
# - Service: spring-ai-chat-memory
# - Operation: GET /weather
# - Tags: http.url, http.status_code 등 확인
```

### Log 확인
```bash
# 1. Grafana Loki에서 실시간 로그 스트리밍
{service_name="spring-ai-chat-memory"} | json

# 2. 특정 시간대 필터
{service_name="spring-ai-chat-memory"} | json | __timestamp__ > now()-15m
```

---

## 🎯 대시보드 생성

### 1. 사전 빌드 대시보드 Import

1. **Grafana** → **Dashboards** → **Import**
2. Dashboard ID 입력:
   - **Spring Boot 2.1+**: `12900`
   - **JVM (Micrometer)**: `4701`
   - **OTEL Collector**: `15983`

### 2. 커스텀 대시보드 패널 예시

#### Panel 1: Request Rate
```promql
sum(rate(http_server_requests_seconds_count{service_name="spring-ai-chat-memory"}[5m])) by (uri, method)
```

#### Panel 2: Error Rate
```promql
sum(rate(http_server_requests_seconds_count{service_name="spring-ai-chat-memory",status=~"5.."}[5m])) 
/ 
sum(rate(http_server_requests_seconds_count{service_name="spring-ai-chat-memory"}[5m]))
```

#### Panel 3: Recent Logs (Loki)
```logql
{service_name="spring-ai-chat-memory"} | json | level="ERROR" or level="WARN"
```

---

## 🐛 트러블슈팅

### 데이터가 안 보일 때

1. **Time Range 확인**: "Last 15 minutes" 또는 "Last 1 hour"로 설정
2. **Service Name 확인**: 정확히 `spring-ai-chat-memory`인지 확인
3. **데이터 소스 연결 확인**:
   ```bash
   # Tempo
   kubectl get svc -n observability | grep tempo
   
   # Loki
   kubectl get svc -n observability | grep loki
   ```

4. **Alloy 로그 확인**:
   ```bash
   kubectl logs -n observability -l app.kubernetes.io/name=alloy-receiver -c alloy --tail=100
   ```

---

## 📝 유용한 LogQL 쿼리

```logql
# 1. 에러 로그만 (최근 1시간)
{service_name="spring-ai-chat-memory"} |= "ERROR" | json

# 2. 특정 클래스 로그
{service_name="spring-ai-chat-memory"} | json | logger_name=~".*WeatherService.*"

# 3. HTTP 요청 로그
{service_name="spring-ai-chat-memory"} |= "GET" or "POST" | json | http_method!=""

# 4. 응답 시간이 긴 요청 (>1초)
{service_name="spring-ai-chat-memory"} | json | duration > 1000

# 5. Trace ID로 검색 (특정 trace의 모든 로그)
{service_name="spring-ai-chat-memory"} | json | trace_id="your-trace-id-here"
```

---

## 🔗 Trace와 Log 연결

1. **Tempo에서 Trace 선택**
2. Trace 상세 화면에서 **Trace ID** 복사
3. **Loki로 이동**
4. 다음 쿼리로 해당 trace의 모든 로그 검색:
   ```logql
   {service_name="spring-ai-chat-memory"} | json | trace_id="복사한-trace-id"
   ```

---

## 📊 추천 대시보드 구성

### Row 1: Overview
- Total Requests (last 1h)
- Error Rate (%)
- Average Response Time
- Active Users

### Row 2: HTTP Metrics
- Request Rate by Endpoint (Graph)
- Response Time Distribution (Heatmap)
- HTTP Status Codes (Pie Chart)

### Row 3: JVM Metrics
- Heap Memory Usage
- GC Pause Time
- Thread Count
- CPU Usage

### Row 4: Recent Logs & Traces
- Error Logs (Loki Panel)
- Recent Traces (Tempo Panel)
- Slow Requests (Tempo Panel, duration > 1s)

---

## 🚀 다음 단계

1. **Alert 설정**: Prometheus/Loki Alert Rules 생성
2. **SLO 모니터링**: Error Budget, Latency SLO 설정
3. **분산 추적**: 여러 서비스 간 trace 연결
4. **커스텀 메트릭**: 비즈니스 메트릭 추가

---

## 참고 자료

- [Grafana Tempo 문서](https://grafana.com/docs/tempo/latest/)
- [Grafana Loki 문서](https://grafana.com/docs/loki/latest/)
- [LogQL 치트시트](https://grafana.com/docs/loki/latest/logql/)
- [PromQL 치트시트](https://promlabs.com/promql-cheat-sheet/)
