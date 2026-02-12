# Langfuse Helm 설치

## 사전 준비

1. Helm 3.x 설치 확인:
```bash
helm version
```

2. kubectl이 클러스터에 연결되어 있는지 확인:
```bash
kubectl cluster-info
```

## 설치 단계

### 1. 네임스페이스 생성 (선택사항)

```bash
kubectl create namespace langfuse
```

### 2. values.yaml 설정

`values.yaml` 파일을 편집하여 필요한 설정을 변경합니다:

- **필수 설정**:
  - `langfuse.salt.value`: 보안 Salt 값
  - `langfuse.nextauth.secret.value`: NextAuth 시크릿 (32자 랜덤 문자열)
  - 데이터베이스 비밀번호들

- **선택 설정**:
  - 리소스 제한
  - 스토리지 크기
  - Ingress 설정

### 3. 시크릿 생성

보안을 위해 민감한 정보는 별도 시크릿으로 관리하는 것을 권장합니다:

```bash
# NextAuth 시크릿 생성
NEXTAUTH_SECRET=$(openssl rand -hex 32)
echo "Generated NEXTAUTH_SECRET: $NEXTAUTH_SECRET"

# Salt 값 생성
SALT=$(openssl rand -hex 16)
echo "Generated SALT: $SALT"
```

### 4. Helm 차트 설치

```bash
# Helm 저장소 추가
helm repo add langfuse https://langfuse.github.io/langfuse-k8s
helm repo update

# 설치
helm install langfuse langfuse/langfuse \
  --namespace langfuse \
  --create-namespace \
  -f values.yaml

# 또는 install.sh 스크립트 사용
chmod +x install.sh
./install.sh
```

### 5. 설치 확인

```bash
# Pod 상태 확인
kubectl get pods -n langfuse

# 서비스 확인
kubectl get svc -n langfuse

# Ingress 확인 (설정한 경우)
kubectl get ingress -n langfuse
```

### 6. 접속

Port-forward를 사용하여 로컬에서 접속:

```bash
kubectl port-forward -n langfuse svc/langfuse 3000:3000
```

브라우저에서 `http://localhost:3000` 접속

## 업그레이드

```bash
helm repo update
helm upgrade langfuse langfuse/langfuse \
  --namespace langfuse \
  -f values.yaml
```

## 삭제

```bash
helm uninstall langfuse --namespace langfuse
kubectl delete namespace langfuse
```

## 트러블슈팅

### Pod가 시작되지 않는 경우

```bash
kubectl describe pod <pod-name> -n langfuse
kubectl logs <pod-name> -n langfuse
```

### 데이터베이스 연결 문제

1. PostgreSQL Pod 상태 확인
2. 비밀번호 설정 확인
3. 네트워크 정책 확인

### 리소스 부족

values.yaml에서 리소스 요구사항을 조정하세요.
