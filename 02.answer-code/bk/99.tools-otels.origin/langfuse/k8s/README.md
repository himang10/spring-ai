# Langfuse Kubernetes Manifest 설치

kubectl을 사용한 순수 Kubernetes manifest 방식 설치 가이드입니다.

## 사전 준비

1. kubectl 설치 및 클러스터 연결 확인:
```bash
kubectl cluster-info
```

2. 필요한 시크릿 값 생성:
```bash
# NextAuth 시크릿 생성 (32자 hex)
openssl rand -hex 32

# Salt 값 생성
openssl rand -hex 16
```

## 설치 순서

### 1. Secret 파일 수정

`secret.yaml` 파일을 편집하여 Base64로 인코딩된 시크릿 값을 입력합니다:

```bash
# 예시: 문자열을 Base64로 인코딩
echo -n "your-password" | base64
```

필수 시크릿:
- `nextauth-secret`: NextAuth 시크릿 (32자)
- `salt`: Salt 값
- `postgres-password`: PostgreSQL 비밀번호
- `redis-password`: Redis 비밀번호

### 2. ConfigMap 수정

`configmap.yaml` 파일에서 다음 항목을 수정합니다:
- `NEXTAUTH_URL`: 실제 도메인 또는 IP
- 기타 환경 변수

### 3. 리소스 배포

파일 순서대로 배포:

```bash
# 1. 네임스페이스
kubectl apply -f namespace.yaml

# 2. 설정 및 시크릿
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml

# 3. 스토리지 (선택사항 - PV를 사용하는 경우)
# kubectl apply -f storage.yaml

# 4. PostgreSQL
kubectl apply -f postgresql.yaml

# 5. Redis
kubectl apply -f redis.yaml

# 6. Langfuse 애플리케이션
kubectl apply -f langfuse-deployment.yaml
kubectl apply -f langfuse-service.yaml

# 7. Ingress (선택사항)
# kubectl apply -f ingress.yaml
```

또는 한 번에 모두 배포:

```bash
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f postgresql.yaml
kubectl apply -f redis.yaml
kubectl apply -f langfuse-deployment.yaml
kubectl apply -f langfuse-service.yaml
```

### 4. 설치 확인

```bash
# Pod 상태 확인
kubectl get pods -n langfuse

# 모든 Pod가 Running 상태가 될 때까지 대기
kubectl wait --for=condition=ready pod --all -n langfuse --timeout=300s

# 로그 확인
kubectl logs -n langfuse deployment/langfuse -f
```

### 5. 접속

#### Port-forward 사용:

```bash
kubectl port-forward -n langfuse svc/langfuse 3000:3000
```

브라우저에서 `http://localhost:3000` 접속

#### NodePort 사용 (서비스 타입 변경 필요):

```bash
kubectl get svc -n langfuse langfuse
```

#### Ingress 사용:

```bash
# Ingress 배포
kubectl apply -f ingress.yaml

# Ingress 상태 확인
kubectl get ingress -n langfuse
```

## 업데이트

이미지 버전을 변경하거나 설정을 업데이트한 후:

```bash
kubectl apply -f <변경된-파일>.yaml

# 또는 배포 재시작
kubectl rollout restart deployment/langfuse -n langfuse
```

## 삭제

```bash
# 모든 리소스 삭제
kubectl delete -f langfuse-service.yaml
kubectl delete -f langfuse-deployment.yaml
kubectl delete -f redis.yaml
kubectl delete -f postgresql.yaml
kubectl delete -f secret.yaml
kubectl delete -f configmap.yaml

# PVC 삭제 (선택사항, 데이터 영구 삭제)
kubectl delete pvc --all -n langfuse

# 네임스페이스 삭제
kubectl delete -f namespace.yaml
```

또는:

```bash
kubectl delete namespace langfuse
```

## 트러블슈팅

### Pod가 CrashLoopBackOff 상태

```bash
# 로그 확인
kubectl logs -n langfuse <pod-name>

# Pod 상세 정보
kubectl describe pod -n langfuse <pod-name>
```

### 데이터베이스 연결 실패

1. PostgreSQL Pod가 정상 실행 중인지 확인
2. 시크릿의 비밀번호가 올바른지 확인
3. 서비스 이름과 포트가 올바른지 확인

### 이미지 Pull 실패

```bash
# 이미지 Pull 시크릿 생성 (Private registry 사용 시)
kubectl create secret docker-registry regcred \
  --docker-server=<registry-url> \
  --docker-username=<username> \
  --docker-password=<password> \
  -n langfuse
```

### 리소스 부족

deployment 파일에서 리소스 요구사항을 조정하세요.

## 고급 설정

### 외부 PostgreSQL 사용

`postgresql.yaml` 배포를 건너뛰고, `configmap.yaml`에서 외부 데이터베이스 연결 정보를 설정합니다.

### 외부 Redis 사용

`redis.yaml` 배포를 건너뛰고, Redis 연결 정보를 ConfigMap에 설정합니다.

### TLS/HTTPS 설정

Ingress에서 cert-manager를 사용하여 자동으로 TLS 인증서를 발급받을 수 있습니다.

```yaml
annotations:
  cert-manager.io/cluster-issuer: "letsencrypt-prod"
```
