# Loki 쿼리 가이드

## 📊 검증 완료: OTEL 환경변수 설정 및 Spring Boot 재시작

### ✅ 확인된 사항

1. **run-with-alloy.sh 스크립트 정상 작동**
   ```bash
   📁 .env.alloy 파일 발견 - 환경변수 로드 중...
   ✅ .env.alloy 파일에서 환경변수 로드 완료
   ```

2. **OTEL 환경변수 로드 확인**
   ```
   OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
   OTEL_EXPORTER_OTLP_PROTOCOL=grpc
   OTEL_SERVICE_NAME=spring-ai-tools
   OTEL_TRACES_EXPORTER=otlp
   OTEL_LOGS_EXPORTER=otlp
   OTEL_METRICS_EXPORTER=otlp
   ```

3. **OpenTelemetry gRPC Exporter 초기화**
   ```
   Using GrpcSender: io.opentelemetry.exporter.sender.okhttp.internal.OkHttpGrpcSender
   ```

4. **Port-forward 정상 실행**
   ```bash
   kubectl port-forward svc/grafana-k8s-monitoring-alloy-receiver 4317:4317 4318:4318
   ✅ 포트 4317 연결 가능
   ```

5. **Spring Boot 애플리케이션 정상 시작**
   ```
   Started MyFirstSpringAiApplication in 3.995 seconds
   Tomcat started on port 8080
   ```

### ⚠️ 현재 상태

- **로그가 Loki에 수집되지 않음**
- Alloy receiver는 메트릭 관련 에러만 표시
- 로그나 트레이스 수신 흔적 없음

### 🔍 원인 분석

**가능한 원인:**

1. **Logback Appender 설정 문제**
   - OpenTelemetry Logback Appender가 활성화되지 않았을 수 있음
   - `logback-spring.xml` 설정 확인 필요

2. **Spring Boot Starter 방식의 제한**
   - 현재 `opentelemetry-spring-boot-starter` 사용 중
   - Java Agent와 달리 자동 로그 수집이 제한적일 수 있음

3. **Alloy receiver 설정**
   - Alloy가 OTLP 로그를 제대로 처리하지 못할 수 있음
   - Loki 전달 파이프라인 문제 가능성

### ✅ 해결 방법

#### 방법 1: Logback 설정 확인 및 수정

`logback-spring.xml` 파일에 OpenTelemetry Appender 추가:

```xml
<appender name="OTEL" class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    <captureExperimentalAttributes>true</captureExperimentalAttributes>
    <captureCodeAttributes>true</captureCodeAttributes>
</appender>

<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="OTEL"/>
</root>
```

#### 방법 2: Java Agent 방식 사용

더 포괄적인 자동 계측을 위해 Java Agent 사용:

1. **pom.xml에 Java Agent 다운로드 추가**:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.googlecode.maven-download-plugin</groupId>
            <artifactId>download-maven-plugin</artifactId>
            <version>1.9.0</version>
            <executions>
                <execution>
                    <id>download-opentelemetry-javaagent</id>
                    <phase>prepare-package</phase>
                    <goals>
                        <goal>wget</goal>
                    </goals>
                    <configuration>
                        <url>https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.11.0/opentelemetry-javaagent.jar</url>
                        <outputDirectory>${project.build.directory}</outputDirectory>
                        <outputFileName>opentelemetry-javaagent.jar</outputFileName>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

2. **run-with-alloy.sh 수정**:
```bash
# 기존
mvn spring-boot:run

# 변경
java -javaagent:target/opentelemetry-javaagent.jar \
  -jar target/spring-ai-tools-0.0.1-SNAPSHOT.jar
```

#### 방법 3: Kubernetes에 배포

로컬 환경의 제약 없이 실제 프로덕션과 동일한 환경에서 테스트:

```bash
# 빌드 및 배포
./docker-build.sh
./docker-push.sh
kubectl apply -f k8s/

# 로그 확인 (1-2분 후)
curl -s 'https://loki.skala25a.project.skala-ai.com/loki/api/v1/label/service_name/values' | jq -r '.data[]' | grep spring
```

---

## ❌ 문제 원인

**OTEL 환경변수가 설정되지 않은 상태로 Spring Boot 실행**
- 현재 Spring Boot는 실행 중이지만 OpenTelemetry 설정이 없음
- 로그가 Alloy로 전송되지 않음

### ✅ 해결 방법

```bash
# 1. 현재 실행 중인 Spring Boot 중지 (Ctrl+C)

# 2. 환경변수 로드
export $(cat .env.alloy | grep -v '^#' | xargs)

# 3. 환경변수 확인
env | grep OTEL

# 4. 재시작
./run-with-alloy.sh
```

---

## 🔍 외부 Loki 접속 (HTTPS)

Loki 외부 접속 URL: `https://loki.skala25a.project.skala-ai.com`

### 1. 수집된 모든 라벨 확인

```bash
curl -s "https://loki.skala25a.project.skala-ai.com/loki/api/v1/labels" | jq -r '.data[]'
```

**결과:**
```
__stream_shard__
app_kubernetes_io_name
cluster
container
instance
job
k8s_cluster_name
level
namespace
node
reason
service_name
service_namespace
source
unit
```

### 2. 네임스페이스 목록 확인

```bash
curl -s "https://loki.skala25a.project.skala-ai.com/loki/api/v1/label/namespace/values" | jq -r '.data[]'
```

**확인된 네임스페이스:**
- skala-practice ✅
- observability
- kafka
- postgres
- mongodb
- 등...

### 3. Job 목록 확인

```bash
curl -s "https://loki.skala25a.project.skala-ai.com/loki/api/v1/label/job/values" | jq -r '.data[]'
```

**확인된 주요 Job:**
```
integrations/kubernetes/eventhandler
integrations/kubernetes/journal
observability/loki
observability/tempo
observability/jaeger-collector
```

### 4. 특정 네임스페이스의 로그 확인

```bash
curl -s 'https://loki.skala25a.project.skala-ai.com/loki/api/v1/query_range?query=%7Bnamespace%3D%22skala-practice%22%7D&limit=10' | jq -r '.data.result[].stream'
```

---

## 💡 Grafana에서 사용할 LogQL 쿼리

### 기본 쿼리

```logql
# 1. 서비스 이름으로 검색 (OTEL이 설정된 경우)
{service_name="spring-ai-tools"}

# 2. 네임스페이스로 검색
{namespace="skala-practice"}

# 3. Job으로 검색
{job="integrations/kubernetes/eventhandler"}

# 4. 특정 Pod 검색 (Pod이 배포된 경우)
{namespace="skala-practice", pod=~"spring-ai-tools.*"}
```

### 로그 레벨 필터링

```logql
# ERROR 로그만
{service_name="spring-ai-tools"} |= "ERROR"

# INFO 로그 제외
{service_name="spring-ai-tools"} != "INFO"

# DEBUG 로그 포함
{service_name="spring-ai-tools"} |~ "DEBUG|TRACE"
```

### 특정 키워드 검색

```logql
# OpenTelemetry 관련 로그
{service_name="spring-ai-tools"} |= "OpenTelemetry"

# Spring AI 관련 로그
{service_name="spring-ai-tools"} |~ "Spring AI|ChatModel"

# 에러 스택 추적
{service_name="spring-ai-tools"} |= "Exception"
```

### 시간 범위 검색

```logql
# 최근 5분
{service_name="spring-ai-tools"}[5m]

# 최근 1시간
{service_name="spring-ai-tools"}[1h]
```

### 집계 쿼리

```logql
# 로그 카운트 (분당)
rate({service_name="spring-ai-tools"}[1m])

# 에러 로그 카운트
count_over_time({service_name="spring-ai-tools"} |= "ERROR" [5m])
```

---

## 🧪 테스트 시나리오

### 1. OTEL 설정 후 확인

```bash
# 1. 환경변수 설정
export $(cat .env.alloy | grep -v '^#' | xargs)

# 2. 애플리케이션 시작
./run-with-alloy.sh

# 3. 로그 생성 (API 호출)
curl http://localhost:8080/

# 4. 1-2분 후 Loki 확인
curl -s 'https://loki.skala25a.project.skala-ai.com/loki/api/v1/label/service_name/values' | jq -r '.data[]' | grep spring
```

**기대 결과:** `spring-ai-tools` 또는 설정한 서비스 이름이 나타남

### 2. Grafana Explore에서 확인

1. Grafana 접속
2. **Explore** 메뉴 선택
3. Data source: **Loki** 선택
4. 쿼리 입력:

```logql
{service_name="spring-ai-tools"}
```

5. **Run query** 클릭

---

## 🐛 트러블슈팅

### 문제 1: service_name이 보이지 않음

**원인:** OTEL 환경변수 미설정

**확인:**
```bash
env | grep OTEL
```

**해결:**
```bash
export $(cat .env.alloy | grep -v '^#' | xargs)
mvn spring-boot:run
```

### 문제 2: 로그가 1-2분 후에야 보임

**정상입니다!** OpenTelemetry는 배치 방식으로 전송합니다.
- 기본 배치 주기: 5-10초
- Loki 인덱싱 시간: 추가 30-60초

### 문제 3: Port-forward Connection refused

**확인:**
```bash
ps aux | grep "port-forward.*alloy-receiver" | grep -v grep
```

**재실행:**
```bash
kubectl port-forward -n observability svc/grafana-k8s-monitoring-alloy-receiver 4317:4317 4318:4318 &
```

---

## 📝 API 레퍼런스

### Loki API 엔드포인트

```bash
# 베이스 URL
LOKI_URL="https://loki.skala25a.project.skala-ai.com"

# 라벨 조회
curl -s "$LOKI_URL/loki/api/v1/labels" | jq

# 특정 라벨의 값 조회
curl -s "$LOKI_URL/loki/api/v1/label/{label_name}/values" | jq

# 로그 쿼리 (실시간)
curl -s "$LOKI_URL/loki/api/v1/query?query={selector}" | jq

# 로그 쿼리 (범위)
curl -s "$LOKI_URL/loki/api/v1/query_range?query={selector}&limit=100" | jq
```

### URL 인코딩

LogQL 쿼리를 URL에서 사용할 때:

```bash
# 원본: {namespace="skala-practice"}
# 인코딩: %7Bnamespace%3D%22skala-practice%22%7D

# Python으로 인코딩
python3 -c "import urllib.parse; print(urllib.parse.quote('{namespace=\"skala-practice\"}'))"

# Bash에서 직접
echo '{namespace="skala-practice"}' | jq -sRr @uri
```

---

## ✅ 체크리스트

Spring Boot 애플리케이션에서 Loki로 로그를 전송하려면:

- [ ] Port-forward 실행 (`kubectl port-forward`)
- [ ] `.env.alloy` 파일 확인
- [ ] OTEL 환경변수 설정 (`export $(cat .env.alloy | xargs)`)
- [ ] Spring Boot 재시작
- [ ] API 호출하여 로그 생성
- [ ] 1-2분 후 Grafana/Loki에서 확인

---

## 🎯 다음 단계

로그가 정상적으로 수집되면:

1. **Grafana Dashboard 생성**
   - Loki data source 사용
   - Logs panel 추가
   - 쿼리 설정

2. **Alert 설정**
   - 에러 로그 임계값 설정
   - Slack/Email 알림 연동

3. **Trace 연동**
   - Tempo에서 trace 확인
   - Loki ↔ Tempo 연결 (TraceID)
   - 통합 뷰 구성
