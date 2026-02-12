# Kubernetes 배포 가이드

Spring AI Tools 애플리케이션을 Kubernetes에 배포하는 가이드입니다.

## 📋 파일 구조

```
k8s/
├── secret.yaml           # API 키 Secret (템플릿)
├── deployment.yaml       # Deployment 리소스
├── service.yaml          # Service 리소스
├── ingress.yaml          # Ingress 리소스
├── create-secret.sh      # Secret 생성 헬퍼 스크립트
└── README.md            # 이 파일
```

## 🚀 빠른 배포 (5단계)

### 1단계: Docker 이미지 빌드

```bash
# 프로젝트 루트 디렉토리에서
docker build -t spring-ai-tools:latest .

# Docker Registry에 푸시 (선택사항)
docker tag spring-ai-tools:latest your-registry/spring-ai-tools:latest
docker push your-registry/spring-ai-tools:latest
```

### 2단계: Secret 생성

#### 방법 A) 대화형 스크립트 사용 (추천)

```bash
cd k8s
./create-secret.sh
```

스크립트가 API 키를 물어봅니다:
- OpenAI API Key
- Weather API Key
- Langfuse Public Key (선택)
- Langfuse Secret Key (선택)

#### 방법 B) kubectl 명령 직접 사용

```bash
# Langfuse Base64 인코딩
AUTH=$(echo -n "pk-lf-YOUR_PUBLIC:sk-lf-YOUR_SECRET" | base64)

# Secret 생성
kubectl create secret generic spring-ai-secrets \
  --from-literal=OPEN_AI_KEY="sk-xxxxx..." \
  --from-literal=WEATHER_API_KEY="your-weather-key" \
  --from-literal=OTEL_EXPORTER_OTLP_ENDPOINT="https://langfuse.skala25a.project.skala-ai.com/api/public/otel" \
  --from-literal=OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH" \
  --namespace=default
```

#### 방법 C) YAML 파일 수정 후 적용

```bash
# secret.yaml 파일을 실제 값으로 수정
vim k8s/secret.yaml

# 적용
kubectl apply -f k8s/secret.yaml
```

### 3단계: Deployment 수정

`k8s/deployment.yaml` 파일에서 이미지 경로 수정:

```yaml
spec:
  containers:
  - name: spring-ai
    image: your-registry/spring-ai-tools:latest  # ← 이미지 경로 수정
```

### 4단계: Ingress 수정 (선택)

외부 접근이 필요한 경우 `k8s/ingress.yaml` 수정:

```yaml
spec:
  rules:
  - host: spring-ai.your-domain.com  # ← 실제 도메인으로 변경
```

### 5단계: 배포

```bash
# Deployment, Service, Ingress 배포
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml  # 선택사항
```

## ✅ 배포 확인

### Pod 상태 확인

```bash
# Pod 목록
kubectl get pods -l app=spring-ai-tools

# Pod 상세 정보
kubectl describe pod <pod-name>

# 로그 확인
kubectl logs -f <pod-name>
```

### Service 확인

```bash
# Service 목록
kubectl get svc spring-ai-tools

# Service 상세 정보
kubectl describe svc spring-ai-tools
```

### Ingress 확인

```bash
# Ingress 목록
kubectl get ingress spring-ai-tools

# Ingress 상세 정보
kubectl describe ingress spring-ai-tools
```

## 🔧 환경변수 설정

### Secret에 저장되는 환경변수

| 환경변수 | 설명 | 필수 |
|---------|------|------|
| `OPEN_AI_KEY` | OpenAI API 키 | ✅ 필수 |
| `WEATHER_API_KEY` | Weather API 키 | ⚠️ 선택 (날씨 기능 사용 시) |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Langfuse OTLP 엔드포인트 | ⚠️ 선택 (Langfuse 사용 시) |
| `OTEL_EXPORTER_OTLP_HEADERS` | Langfuse 인증 헤더 (Base64) | ⚠️ 선택 (Langfuse 사용 시) |

### Secret 업데이트

```bash
# Secret 삭제
kubectl delete secret spring-ai-secrets -n default

# 재생성
./create-secret.sh
```

또는 직접 수정:

```bash
# Secret 편집
kubectl edit secret spring-ai-secrets -n default
```

⚠️ **주의**: Secret의 값은 Base64로 인코딩되어 있습니다.

## 🎯 Health Check 엔드포인트

애플리케이션은 다음 엔드포인트를 제공합니다:

| 엔드포인트 | 용도 |
|-----------|------|
| `/actuator/health` | 전체 상태 |
| `/actuator/health/liveness` | Liveness Probe |
| `/actuator/health/readiness` | Readiness Probe |

## 🔍 트러블슈팅

### 1. Pod가 시작되지 않음

```bash
# Pod 이벤트 확인
kubectl describe pod <pod-name>

# Pod 로그 확인
kubectl logs <pod-name>

# 이전 컨테이너 로그 확인 (재시작된 경우)
kubectl logs <pod-name> --previous
```

**일반적인 원인**:
- Secret이 생성되지 않음 → `kubectl get secret spring-ai-secrets`
- 이미지를 Pull할 수 없음 → 이미지 경로 확인
- 환경변수 오류 → Secret 값 확인

### 2. Health Check 실패

```bash
# Health 엔드포인트 직접 테스트
kubectl port-forward <pod-name> 8080:8080
curl http://localhost:8080/actuator/health
```

### 3. Langfuse 연동 안 됨

```bash
# 환경변수 확인
kubectl exec <pod-name> -- env | grep OTEL

# Langfuse 엔드포인트 테스트
kubectl exec <pod-name> -- curl -I https://langfuse.skala25a.project.skala-ai.com
```

**체크리스트**:
- [ ] Secret에 `OTEL_EXPORTER_OTLP_ENDPOINT` 설정됨
- [ ] Secret에 `OTEL_EXPORTER_OTLP_HEADERS` 설정됨
- [ ] Base64 인코딩이 올바름
- [ ] Langfuse 서버가 실행 중

### 4. Ingress 접속 안 됨

```bash
# Ingress 확인
kubectl get ingress spring-ai-tools
kubectl describe ingress spring-ai-tools

# Service 확인
kubectl get svc spring-ai-tools
kubectl get endpoints spring-ai-tools
```

## 🔒 보안 권장사항

### 1. Secret 관리

```bash
# Secret을 Git에 커밋하지 마세요!
echo "k8s/secret.yaml" >> .gitignore

# 또는 SealedSecrets 사용
# https://github.com/bitnami-labs/sealed-secrets
```

### 2. RBAC 설정

최소 권한 원칙을 따르는 ServiceAccount 생성:

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spring-ai-sa
  namespace: default
```

Deployment에 추가:
```yaml
spec:
  template:
    spec:
      serviceAccountName: spring-ai-sa
```

### 3. Network Policy

필요한 트래픽만 허용:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: spring-ai-netpol
spec:
  podSelector:
    matchLabels:
      app: spring-ai-tools
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector: {}
    ports:
    - port: 8080
  egress:
  - to:
    - podSelector: {}
  - ports:
    - port: 443  # HTTPS
    - port: 53   # DNS
```

## 📊 모니터링

### Prometheus ServiceMonitor (선택)

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: spring-ai-tools
  labels:
    app: spring-ai-tools
spec:
  selector:
    matchLabels:
      app: spring-ai-tools
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

## 🔄 업데이트 및 롤백

### 새 버전 배포

```bash
# 새 이미지 빌드
docker build -t spring-ai-tools:v2 .
docker push your-registry/spring-ai-tools:v2

# Deployment 이미지 업데이트
kubectl set image deployment/spring-ai-tools spring-ai=your-registry/spring-ai-tools:v2

# 롤아웃 상태 확인
kubectl rollout status deployment/spring-ai-tools
```

### 롤백

```bash
# 이전 버전으로 롤백
kubectl rollout undo deployment/spring-ai-tools

# 특정 버전으로 롤백
kubectl rollout undo deployment/spring-ai-tools --to-revision=1

# 롤아웃 히스토리 확인
kubectl rollout history deployment/spring-ai-tools
```

## 📚 참고 자료

- [Kubernetes 공식 문서](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [Langfuse Kubernetes 배포](../langfuse/helm/README.md)

## 💡 팁

### 로컬 Kubernetes 테스트

```bash
# Minikube 사용
minikube start
eval $(minikube docker-env)
docker build -t spring-ai-tools:latest .
kubectl apply -f k8s/

# Kind 사용
kind create cluster
kind load docker-image spring-ai-tools:latest
kubectl apply -f k8s/
```

### ConfigMap으로 설정 관리

민감하지 않은 설정은 ConfigMap으로:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: spring-ai-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  JAVA_OPTS: "-Xmx512m"
```

---

**요약**:
```bash
./create-secret.sh                 # Secret 생성
kubectl apply -f k8s/              # 모든 리소스 배포
kubectl get pods -l app=spring-ai-tools  # 확인
```
