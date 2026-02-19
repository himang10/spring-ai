#!/bin/bash

# Langfuse Kubernetes Manifest 설치 스크립트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수 정의
error() {
    echo -e "${RED}ERROR: $1${NC}"
    exit 1
}

info() {
    echo -e "${GREEN}INFO: $1${NC}"
}

warn() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

step() {
    echo -e "${BLUE}===> $1${NC}"
}

# 변수
NAMESPACE="langfuse"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

info "Langfuse Kubernetes 설치 시작..."
echo ""

# 1. 사전 요구사항 확인
step "1. 사전 요구사항 확인"

if ! command -v kubectl &> /dev/null; then
    error "kubectl이 설치되어 있지 않습니다."
fi

if ! kubectl cluster-info &> /dev/null; then
    error "Kubernetes 클러스터에 연결할 수 없습니다."
fi

info "사전 요구사항 확인 완료"
echo ""

# 2. 시크릿 생성 도움말
step "2. 시크릿 값 생성"

warn "secret.yaml 파일의 시크릿 값을 수정해야 합니다."
echo ""
echo "다음 명령어로 시크릿 값을 생성할 수 있습니다:"
echo ""
echo "  # NextAuth 시크릿 (32자 hex)"
echo "  openssl rand -hex 32"
echo ""
echo "  # Salt 값"
echo "  openssl rand -hex 16"
echo ""
echo "  # 비밀번호를 Base64로 인코딩"
echo "  echo -n 'your-password' | base64"
echo ""

read -p "secret.yaml 파일을 수정하셨나요? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    warn "secret.yaml 파일을 먼저 수정해주세요."
    exit 0
fi

# 3. ConfigMap 수정 확인
warn "configmap.yaml에서 NEXTAUTH_URL을 실제 도메인으로 변경하셨나요?"
read -p "계속 진행하시겠습니까? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    info "설치를 취소했습니다."
    exit 0
fi

echo ""
step "3. Kubernetes 리소스 배포"

# 네임스페이스
info "네임스페이스 생성 중..."
kubectl apply -f "$SCRIPT_DIR/namespace.yaml"

# ConfigMap과 Secret
info "ConfigMap 및 Secret 생성 중..."
kubectl apply -f "$SCRIPT_DIR/configmap.yaml"
kubectl apply -f "$SCRIPT_DIR/secret.yaml"

# PostgreSQL
info "PostgreSQL 배포 중..."
kubectl apply -f "$SCRIPT_DIR/postgresql.yaml"

# Redis
info "Redis 배포 중..."
kubectl apply -f "$SCRIPT_DIR/redis.yaml"

# 데이터베이스가 준비될 때까지 대기
info "데이터베이스 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=postgres -n "$NAMESPACE" --timeout=300s || warn "PostgreSQL Pod가 준비되지 않았습니다. 계속 진행합니다..."
kubectl wait --for=condition=ready pod -l app=redis -n "$NAMESPACE" --timeout=300s || warn "Redis Pod가 준비되지 않았습니다. 계속 진행합니다..."

# Langfuse
info "Langfuse 배포 중..."
kubectl apply -f "$SCRIPT_DIR/langfuse-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/langfuse-service.yaml"

# Ingress (선택사항)
if [ -f "$SCRIPT_DIR/ingress.yaml" ]; then
    read -p "Ingress를 배포하시겠습니까? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        info "Ingress 배포 중..."
        kubectl apply -f "$SCRIPT_DIR/ingress.yaml"
    fi
fi

echo ""
step "4. 배포 상태 확인"

info "Pod 상태 확인 중..."
kubectl get pods -n "$NAMESPACE"

echo ""
info "Langfuse Pod가 준비될 때까지 대기 중..."
kubectl wait --for=condition=ready pod -l app=langfuse -n "$NAMESPACE" --timeout=300s || warn "Langfuse Pod가 아직 준비되지 않았습니다."

echo ""
step "5. 설치 완료!"

echo ""
echo "=== 배포 상태 ==="
kubectl get all -n "$NAMESPACE"

echo ""
echo "=== 접속 방법 ==="
echo ""
echo "1. Port-forward를 사용한 로컬 접속:"
echo "   kubectl port-forward -n $NAMESPACE svc/langfuse 3000:3000"
echo "   브라우저에서 http://localhost:3000 접속"
echo ""
echo "2. Ingress를 사용한 외부 접속 (설정한 경우):"
echo "   kubectl get ingress -n $NAMESPACE"
echo ""

echo "=== 유용한 명령어 ==="
echo "  - Pod 로그 확인: kubectl logs -n $NAMESPACE -l app=langfuse -f"
echo "  - Pod 상태 확인: kubectl get pods -n $NAMESPACE"
echo "  - 전체 리소스 확인: kubectl get all -n $NAMESPACE"
echo "  - Pod 재시작: kubectl rollout restart deployment/langfuse -n $NAMESPACE"
echo ""

info "설치가 완료되었습니다!"
