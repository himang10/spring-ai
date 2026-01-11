#!/bin/bash

# Langfuse Kubernetes 삭제 스크립트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

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

NAMESPACE="langfuse"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

warn "다음 리소스가 삭제됩니다:"
echo "  - Namespace: $NAMESPACE"
echo "  - 모든 배포, 서비스, PVC"
echo "  - 모든 데이터 (영구 삭제)"
echo ""

read -p "정말 삭제하시겠습니까? (yes/no) " -r
echo
if [[ ! $REPLY == "yes" ]]; then
    info "삭제를 취소했습니다."
    exit 0
fi

info "리소스 삭제 시작..."

# 서비스 삭제
info "서비스 삭제 중..."
kubectl delete -f "$SCRIPT_DIR/langfuse-service.yaml" 2>/dev/null || true

# Ingress 삭제
if [ -f "$SCRIPT_DIR/ingress.yaml" ]; then
    info "Ingress 삭제 중..."
    kubectl delete -f "$SCRIPT_DIR/ingress.yaml" 2>/dev/null || true
fi

# 배포 삭제
info "Deployment 삭제 중..."
kubectl delete -f "$SCRIPT_DIR/langfuse-deployment.yaml" 2>/dev/null || true
kubectl delete -f "$SCRIPT_DIR/redis.yaml" 2>/dev/null || true
kubectl delete -f "$SCRIPT_DIR/postgresql.yaml" 2>/dev/null || true

# ConfigMap과 Secret 삭제
info "ConfigMap 및 Secret 삭제 중..."
kubectl delete -f "$SCRIPT_DIR/secret.yaml" 2>/dev/null || true
kubectl delete -f "$SCRIPT_DIR/configmap.yaml" 2>/dev/null || true

# PVC 삭제 확인
echo ""
warn "PVC를 삭제하면 모든 데이터가 영구적으로 삭제됩니다."
read -p "PVC를 삭제하시겠습니까? (yes/no) " -r
echo
if [[ $REPLY == "yes" ]]; then
    info "PVC 삭제 중..."
    kubectl delete pvc --all -n "$NAMESPACE" 2>/dev/null || true
fi

# 네임스페이스 삭제
echo ""
read -p "네임스페이스를 삭제하시겠습니까? (yes/no) " -r
echo
if [[ $REPLY == "yes" ]]; then
    info "네임스페이스 삭제 중..."
    kubectl delete -f "$SCRIPT_DIR/namespace.yaml" 2>/dev/null || true
fi

info "삭제가 완료되었습니다!"
