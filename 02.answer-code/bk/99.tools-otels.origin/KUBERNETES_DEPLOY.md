# Kubernetes 배포 빠른 시작

Spring AI Tools를 Kubernetes에 배포하는 가장 빠른 방법입니다.

## 🚀 빠른 배포 (3단계)

### 1단계: Secret 생성

```bash
cd k8s
./create-secret.sh
```

입력 사항:
- Namespace: `default` (기본값)
- OpenAI API Key: `sk-xxxxx...`
- Weather API Key: `your-weather-key`
- Langfuse Public Key: `pk-lf-xxxxx...` (선택)
- Langfuse Secret Key: `sk-lf-xxxxx...` (선택)

### 2단계: 빌드 & 배포

```bash
./build-and-deploy.sh
```

또는 수동으로:

```bash
# 빌드
mvn clean package -DskipTests
docker build -t spring-ai-tools:latest .

# 배포
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml  # 선택사항
```

### 3단계: 확인

```bash
# Pod 상태
kubectl get pods -l app=spring-ai-tools

# 로그
kubectl logs -f -l app=spring-ai-tools

# 포트 포워딩 테스트
kubectl port-forward svc/spring-ai-tools 8080:80
curl http://localhost:8080/actuator/health
```

## 📁 파일 구조

```
.
├── k8s/
│   ├── secret.yaml          # Secret 템플릿
│   ├── deployment.yaml      # Deployment (환경변수 주입)
│   ├── service.yaml         # Service
│   ├── ingress.yaml         # Ingress
│   ├── create-secret.sh     # Secret 생성 스크립트
│   └── README.md           # 상세 가이드
├── Dockerfile              # Docker 이미지 빌드
├── .dockerignore           # Docker 빌드 제외 파일
└── build-and-deploy.sh     # 자동 빌드 & 배포 스크립트
```

## 🔧 환경변수 (Secret으로 주입)

[deployment.yaml](k8s/deployment.yaml)에서 다음 환경변수들이 Secret에서 주입됩니다:

### 필수 환경변수

```yaml
env:
# OpenAI API Key
- name: OPEN_AI_KEY
  valueFrom:
    secretKeyRef:
      name: spring-ai-secrets
      key: OPEN_AI_KEY

# Weather API Key
- name: WEATHER_API_KEY
  valueFrom:
    secretKeyRef:
      name: spring-ai-secrets
      key: WEATHER_API_KEY
```

### Langfuse 환경변수 (선택)

```yaml
# Langfuse Endpoint
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  valueFrom:
    secretKeyRef:
      name: spring-ai-secrets
      key: OTEL_EXPORTER_OTLP_ENDPOINT

# Langfuse Authorization Header
- name: OTEL_EXPORTER_OTLP_HEADERS
  valueFrom:
    secretKeyRef:
      name: spring-ai-secrets
      key: OTEL_EXPORTER_OTLP_HEADERS
```

## 🎯 Deployment 주요 설정

### 리소스 제한

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### Health Checks

```yaml
# Liveness Probe
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10

# Readiness Probe
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

## 🔄 업데이트

### 새 버전 배포

```bash
# 빌드 & 배포 (버전 태그 지정)
./build-and-deploy.sh v2

# 또는 수동으로
docker build -t spring-ai-tools:v2 .
kubectl set image deployment/spring-ai-tools spring-ai=spring-ai-tools:v2
kubectl rollout status deployment/spring-ai-tools
```

### 롤백

```bash
# 이전 버전으로
kubectl rollout undo deployment/spring-ai-tools

# 특정 버전으로
kubectl rollout undo deployment/spring-ai-tools --to-revision=1
```

## 🐛 트러블슈팅

### Secret 확인

```bash
# Secret 존재 확인
kubectl get secret spring-ai-secrets

# Secret 내용 확인
kubectl describe secret spring-ai-secrets

# Secret 값 디코딩 (Base64)
kubectl get secret spring-ai-secrets -o jsonpath='{.data.OPEN_AI_KEY}' | base64 -d
```

### Pod 문제

```bash
# Pod 상태
kubectl get pods -l app=spring-ai-tools

# Pod 상세 정보
kubectl describe pod <pod-name>

# 로그
kubectl logs -f <pod-name>

# 이전 로그 (재시작된 경우)
kubectl logs <pod-name> --previous

# Pod 내부 접속
kubectl exec -it <pod-name> -- /bin/sh
```

### 환경변수 확인

```bash
# Pod 내 환경변수 확인
kubectl exec <pod-name> -- env | grep -E 'OPEN_AI|OTEL'
```

## 📚 상세 문서

- [k8s/README.md](k8s/README.md) - 전체 Kubernetes 배포 가이드
- [Dockerfile](Dockerfile) - Docker 이미지 빌드
- [deployment.yaml](k8s/deployment.yaml) - Deployment 설정 (환경변수 주입)

## 💡 스크립트 사용법

### create-secret.sh

대화형으로 Secret 생성:

```bash
cd k8s
./create-secret.sh
```

### build-and-deploy.sh

전체 빌드 및 배포:

```bash
# 기본 배포 (latest 태그, default 네임스페이스)
./build-and-deploy.sh

# 버전 태그 지정
./build-and-deploy.sh v1.0.0

# 네임스페이스 지정
./build-and-deploy.sh latest my-namespace

# Registry 지정 (이미지 푸시)
./build-and-deploy.sh latest default docker.io/myuser
```

## 🔒 보안 권장사항

### 1. Secret을 Git에 커밋하지 마세요

```bash
# .gitignore에 추가되어 있어야 함
k8s/secret.yaml
```

### 2. 프로덕션에서는 SealedSecrets 사용

```bash
# SealedSecrets 설치
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.18.0/controller.yaml

# Secret을 SealedSecret으로 변환
kubeseal -f k8s/secret.yaml -w k8s/sealed-secret.yaml

# SealedSecret 배포 (안전하게 Git에 커밋 가능)
kubectl apply -f k8s/sealed-secret.yaml
```

### 3. RBAC 적용

최소 권한 원칙으로 ServiceAccount 생성

## 🌐 외부 접근

### Port Forward (개발)

```bash
kubectl port-forward svc/spring-ai-tools 8080:80
```

브라우저: http://localhost:8080

### Ingress (프로덕션)

[k8s/ingress.yaml](k8s/ingress.yaml) 수정:

```yaml
spec:
  rules:
  - host: spring-ai.your-domain.com  # 실제 도메인
```

배포:

```bash
kubectl apply -f k8s/ingress.yaml
```

## 📊 모니터링

### 애플리케이션 로그

```bash
# 실시간 로그
kubectl logs -f -l app=spring-ai-tools

# 최근 로그
kubectl logs --tail=100 -l app=spring-ai-tools
```

### 메트릭

```bash
# Actuator 엔드포인트
kubectl port-forward svc/spring-ai-tools 8080:80
curl http://localhost:8080/actuator
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

## 🎉 완료!

배포가 완료되면 다음과 같이 확인할 수 있습니다:

```bash
# 1. Pod 확인
kubectl get pods -l app=spring-ai-tools

# 2. Service 확인
kubectl get svc spring-ai-tools

# 3. 로그 확인
kubectl logs -f -l app=spring-ai-tools

# 4. Health 확인
kubectl port-forward svc/spring-ai-tools 8080:80
curl http://localhost:8080/actuator/health
```

---

**요약**:
```bash
cd k8s && ./create-secret.sh && cd ..  # Secret 생성
./build-and-deploy.sh                   # 빌드 & 배포
kubectl get pods -l app=spring-ai-tools # 확인
```
