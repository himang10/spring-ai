#!/bin/bash

# Langfuse Helm 설치 스크립트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 함수: 에러 메시지 출력
error() {
    echo -e "${RED}ERROR: $1${NC}"
    exit 1
}

# 함수: 정보 메시지 출력
info() {
    echo -e "${GREEN}INFO: $1${NC}"
}

# 함수: 경고 메시지 출력
warn() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

# 변수 설정
NAMESPACE=${LANGFUSE_NAMESPACE:-"langfuse"}
RELEASE_NAME=${LANGFUSE_RELEASE_NAME:-"langfuse"}
VALUES_FILE=${LANGFUSE_VALUES_FILE:-"values.yaml"}

info "Langfuse Kubernetes 설치 시작..."

# 1. 사전 요구사항 확인
info "사전 요구사항 확인 중..."

# kubectl 확인
if ! command -v kubectl &> /dev/null; then
    error "kubectl이 설치되어 있지 않습니다."
fi

# helm 확인
if ! command -v helm &> /dev/null; then
    error "helm이 설치되어 있지 않습니다."
fi

# 클러스터 연결 확인
if ! kubectl cluster-info &> /dev/null; then
    error "Kubernetes 클러스터에 연결할 수 없습니다."
fi

info "사전 요구사항 확인 완료"

# 2. values.yaml 파일 확인
if [ ! -f "$VALUES_FILE" ]; then
    error "values.yaml 파일을 찾을 수 없습니다: $VALUES_FILE"
fi

# 3. 시크릿 값 검증
warn "values.yaml 파일의 시크릿 값을 확인해주세요:"
echo "  - langfuse.salt.value"
echo "  - langfuse.nextauth.secret.value"
echo "  - postgresql.auth.password"
echo "  - clickhouse.auth.password"
echo "  - redis.auth.password"
echo "  - s3.auth.rootPassword"
echo ""

read -p "계속 진행하시겠습니까? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    info "설치를 취소했습니다."
    exit 0
fi

# 4. 네임스페이스 생성
info "네임스페이스 생성 중: $NAMESPACE"
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    kubectl create namespace "$NAMESPACE"
    info "네임스페이스 생성 완료: $NAMESPACE"
else
    info "네임스페이스가 이미 존재합니다: $NAMESPACE"
fi

# 5. Helm 저장소 추가
info "Helm 저장소 추가 중..."
helm repo add langfuse https://langfuse.github.io/langfuse-k8s
helm repo update

# 6. Langfuse 설치
info "Langfuse 설치 중..."
helm install "$RELEASE_NAME" langfuse/langfuse \
    --namespace "$NAMESPACE" \
    -f "$VALUES_FILE" \
    --wait \
    --timeout 10m

if [ $? -eq 0 ]; then
    info "Langfuse 설치 완료!"
else
    error "Langfuse 설치 실패"
fi

# 7. 설치 상태 확인
info "설치 상태 확인 중..."
echo ""
echo "=== Pods 상태 ==="
kubectl get pods -n "$NAMESPACE"
echo ""
echo "=== Services ==="
kubectl get svc -n "$NAMESPACE"
echo ""

# 8. 접속 방법 안내
info "접속 방법:"
echo ""
echo "1. Port-forward를 사용한 로컬 접속:"
echo "   kubectl port-forward -n $NAMESPACE svc/$RELEASE_NAME 3000:3000"
echo "   브라우저에서 http://localhost:3000 접속"
echo ""
echo "2. Ingress를 사용한 외부 접속 (설정한 경우):"
echo "   kubectl get ingress -n $NAMESPACE"
echo ""

# 9. 유용한 명령어 안내
info "유용한 명령어:"
echo "  - Pod 로그 확인: kubectl logs -n $NAMESPACE <pod-name>"
echo "  - Pod 상태 확인: kubectl describe pod -n $NAMESPACE <pod-name>"
echo "  - 서비스 확인: kubectl get svc -n $NAMESPACE"
echo "  - Ingress 확인: kubectl get ingress -n $NAMESPACE"
echo "  - Helm 상태 확인: helm status $RELEASE_NAME -n $NAMESPACE"
echo "  - 업그레이드: helm upgrade $RELEASE_NAME langfuse/langfuse -n $NAMESPACE -f $VALUES_FILE"
echo "  - 삭제: helm uninstall $RELEASE_NAME -n $NAMESPACE"
echo ""

info "설치가 완료되었습니다!"
