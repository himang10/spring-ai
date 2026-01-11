# Langfuse Kubernetes 빠른 시작 가이드

Langfuse를 Kubernetes 환경에 빠르게 설치하기 위한 가이드입니다.

## 목차

1. [Helm 방식 (권장)](#helm-방식-권장)
2. [Kubernetes Manifest 방식](#kubernetes-manifest-방식)
3. [설치 후 확인](#설치-후-확인)
4. [트러블슈팅](#트러블슈팅)

---

## Helm 방식 (권장)

### 사전 준비

```bash
# Helm 3.x 설치 확인
helm version

# kubectl 연결 확인
kubectl cluster-info
```

### 1단계: 디렉토리 이동

```bash
cd langfuse/helm
```

### 2단계: values.yaml 수정

```bash
# 시크릿 생성
NEXTAUTH_SECRET=$(openssl rand -hex 32)
SALT=$(openssl rand -hex 16)

echo "NEXTAUTH_SECRET: $NEXTAUTH_SECRET"
echo "SALT: $SALT"

# values.yaml 파일에서 다음 값들을 변경:
# - langfuse.nextauth.secret.value
# - langfuse.salt.value
# - postgresql.auth.password
# - clickhouse.auth.password
# - redis.auth.password
# - s3.auth.rootPassword
```

### 3단계: 설치

```bash
# 설치 스크립트 실행
./install.sh

# 또는 수동 설치
helm repo add langfuse https://langfuse.github.io/langfuse-k8s
helm repo update
helm install langfuse langfuse/langfuse -n langfuse --create-namespace -f values.yaml
```

### 4단계: 접속

```bash
# Port-forward
kubectl port-forward -n langfuse svc/langfuse 3000:3000

# 브라우저에서 http://localhost:3000 접속
```

---

## Kubernetes Manifest 방식

### 1단계: 디렉토리 이동

```bash
cd langfuse/k8s
```

### 2단계: 시크릿 생성

```bash
# 자동 생성 스크립트 사용
./generate-secrets.sh

# 또는 수동으로 생성
openssl rand -hex 32  # NextAuth secret
openssl rand -hex 16  # Salt
echo -n "your-password" | base64  # 비밀번호를 Base64로 인코딩
```

### 3단계: 설정 파일 수정

**secret.yaml**:
- `nextauth-secret`: NextAuth 시크릿 (Base64)
- `salt`: Salt 값 (Base64)
- `postgres-password`: PostgreSQL 비밀번호 (Base64)
- `redis-password`: Redis 비밀번호 (Base64)

**configmap.yaml**:
- `NEXTAUTH_URL`: 실제 도메인 또는 IP 주소

### 4단계: 설치

```bash
# 설치 스크립트 실행
./install.sh

# 또는 수동 설치
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f postgresql.yaml
kubectl apply -f redis.yaml
kubectl apply -f langfuse-deployment.yaml
kubectl apply -f langfuse-service.yaml
```

### 5단계: 접속

```bash
# Port-forward
kubectl port-forward -n langfuse svc/langfuse 3000:3000

# 브라우저에서 http://localhost:3000 접속
```

---

## 설치 후 확인

### 1. Pod 상태 확인

```bash
kubectl get pods -n langfuse
```

모든 Pod가 `Running` 상태여야 합니다.

### 2. 로그 확인

```bash
# Langfuse 로그
kubectl logs -n langfuse -l app=langfuse -f

# PostgreSQL 로그
kubectl logs -n langfuse -l app=postgres

# Redis 로그
kubectl logs -n langfuse -l app=redis
```

### 3. 서비스 확인

```bash
kubectl get svc -n langfuse
```

### 4. 접속 테스트

```bash
# Port-forward
kubectl port-forward -n langfuse svc/langfuse 3000:3000

# 다른 터미널에서
curl http://localhost:3000/api/public/health
```

---

## 외부 접속 설정

### 1. NodePort 사용

**langfuse-service.yaml** 수정:

```yaml
spec:
  type: NodePort
  ports:
    - port: 3000
      targetPort: 3000
      nodePort: 30000  # 30000-32767 범위
```

접속: `http://<node-ip>:30000`

### 2. LoadBalancer 사용

**langfuse-service.yaml** 수정:

```yaml
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 3000
```

```bash
kubectl get svc -n langfuse langfuse
# EXTERNAL-IP 확인 후 접속
```

### 3. Ingress 사용 (권장)

```bash
# Ingress 배포
kubectl apply -f ingress.yaml

# Ingress 확인
kubectl get ingress -n langfuse
```

**ingress.yaml** 수정:
- `host`: 실제 도메인
- `tls.secretName`: TLS 인증서 시크릿

---

## 트러블슈팅

### Pod가 시작되지 않음

```bash
# Pod 상세 정보 확인
kubectl describe pod -n langfuse <pod-name>

# 이벤트 확인
kubectl get events -n langfuse --sort-by='.lastTimestamp'
```

### 데이터베이스 연결 실패

1. PostgreSQL Pod가 실행 중인지 확인:
   ```bash
   kubectl get pods -n langfuse -l app=postgres
   ```

2. 비밀번호 확인:
   ```bash
   kubectl get secret -n langfuse langfuse-secrets -o yaml
   ```

3. 서비스 DNS 확인:
   ```bash
   kubectl exec -n langfuse deployment/langfuse -- nslookup postgres
   ```

### 이미지 Pull 실패

```bash
# 이미지 정보 확인
kubectl describe pod -n langfuse <pod-name> | grep -A 5 "Events:"

# 특정 버전 사용 (latest 대신)
# langfuse-deployment.yaml에서 image 태그 변경
# image: langfuse/langfuse:2.0.0
```

### 리소스 부족

리소스 요청/제한을 조정:

```yaml
resources:
  requests:
    cpu: 250m
    memory: 256Mi
  limits:
    cpu: 500m
    memory: 512Mi
```

### Pod가 Pending 상태

```bash
# PVC 상태 확인
kubectl get pvc -n langfuse

# 스토리지 클래스 확인
kubectl get storageclass

# 노드 리소스 확인
kubectl top nodes
```

---

## 유용한 명령어

```bash
# 전체 리소스 확인
kubectl get all -n langfuse

# 로그 스트리밍
kubectl logs -n langfuse -l app=langfuse -f --tail=100

# Pod 재시작
kubectl rollout restart deployment/langfuse -n langfuse

# Pod 디버깅
kubectl exec -it -n langfuse deployment/langfuse -- /bin/sh

# 설정 확인
kubectl get configmap -n langfuse langfuse-config -o yaml

# 시크릿 확인 (Base64 디코딩)
kubectl get secret -n langfuse langfuse-secrets -o json | jq -r '.data | map_values(@base64d)'
```

---

## 업그레이드

### Helm 방식

```bash
cd langfuse/helm
helm repo update
helm upgrade langfuse langfuse/langfuse -n langfuse -f values.yaml
```

### Manifest 방식

```bash
cd langfuse/k8s
kubectl apply -f langfuse-deployment.yaml
kubectl rollout status deployment/langfuse -n langfuse
```

---

## 삭제

### Helm 방식

```bash
cd langfuse/helm
./uninstall.sh

# 또는
helm uninstall langfuse -n langfuse
kubectl delete namespace langfuse
```

### Manifest 방식

```bash
cd langfuse/k8s
./uninstall.sh

# 또는
kubectl delete namespace langfuse
```

---

## 다음 단계

1. **사용자 계정 생성**: 첫 접속 시 관리자 계정 생성
2. **프로젝트 생성**: Langfuse에서 새 프로젝트 생성
3. **API 키 발급**: 프로젝트 설정에서 API 키 생성
4. **애플리케이션 연동**: Spring AI 등에서 Langfuse 연동

### Spring AI 연동 예시

```yaml
# application.yml
spring:
  ai:
    langfuse:
      enabled: true
      public-key: ${LANGFUSE_PUBLIC_KEY}
      secret-key: ${LANGFUSE_SECRET_KEY}
      base-url: http://langfuse.example.com  # 또는 http://localhost:3000
```

---

## 참고 자료

- [Langfuse 공식 문서](https://langfuse.com/docs)
- [Kubernetes Helm 차트](https://github.com/langfuse/langfuse-k8s)
- [Langfuse GitHub](https://github.com/langfuse/langfuse)
