#!/bin/bash

# Langfuse Helm 삭제 스크립트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# 변수 설정
NAMESPACE=${LANGFUSE_NAMESPACE:-"langfuse"}
RELEASE_NAME=${LANGFUSE_RELEASE_NAME:-"langfuse"}

warn "다음 리소스가 삭제됩니다:"
echo "  - Helm Release: $RELEASE_NAME"
echo "  - Namespace: $NAMESPACE"
echo "  - 모든 데이터 (PVC 포함)"
echo ""

read -p "정말 삭제하시겠습니까? (yes/no) " -r
echo
if [[ ! $REPLY == "yes" ]]; then
    info "삭제를 취소했습니다."
    exit 0
fi

# Helm 릴리스 삭제
info "Helm 릴리스 삭제 중..."
if helm list -n "$NAMESPACE" | grep -q "$RELEASE_NAME"; then
    helm uninstall "$RELEASE_NAME" -n "$NAMESPACE"
    info "Helm 릴리스 삭제 완료"
else
    warn "Helm 릴리스를 찾을 수 없습니다: $RELEASE_NAME"
fi

# PVC 삭제 확인
read -p "PVC도 삭제하시겠습니까? (데이터가 영구 삭제됩니다) (yes/no) " -r
echo
if [[ $REPLY == "yes" ]]; then
    info "PVC 삭제 중..."
    kubectl delete pvc --all -n "$NAMESPACE" 2>/dev/null || true
    info "PVC 삭제 완료"
fi

# 네임스페이스 삭제
read -p "네임스페이스도 삭제하시겠습니까? (yes/no) " -r
echo
if [[ $REPLY == "yes" ]]; then
    info "네임스페이스 삭제 중..."
    kubectl delete namespace "$NAMESPACE"
    info "네임스페이스 삭제 완료"
fi

info "삭제가 완료되었습니다!"
