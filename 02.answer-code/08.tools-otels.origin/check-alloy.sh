#!/bin/bash

echo "============================================"
echo "Grafana Alloy 상태 확인"
echo "============================================"
echo ""

# Namespace 찾기 (monitoring, observability, default 등)
NAMESPACES=("monitoring" "observability" "grafana" "default" "kube-system")

echo "1️⃣  Alloy Pod 검색 중..."
echo ""

FOUND=false
for ns in "${NAMESPACES[@]}"; do
    echo "Namespace: $ns"
    PODS=$(kubectl get pods -n $ns -l app.kubernetes.io/name=alloy 2>/dev/null || kubectl get pods -n $ns -l app=alloy 2>/dev/null)

    if [ -n "$PODS" ]; then
        echo "✅ Alloy Pod 발견!"
        echo "$PODS"
        FOUND=true
        ALLOY_NS=$ns
        break
    else
        echo "  (없음)"
    fi
    echo ""
done

if [ "$FOUND" = false ]; then
    echo "❌ Alloy Pod를 찾을 수 없습니다."
    echo ""
    echo "다음 명령으로 수동 확인:"
    echo "  kubectl get pods -A | grep alloy"
    echo ""
    exit 1
fi

echo ""
echo "2️⃣  Alloy Pod 상세 정보"
echo "============================================"
kubectl get pods -n $ALLOY_NS -l app.kubernetes.io/name=alloy -o wide 2>/dev/null || kubectl get pods -n $ALLOY_NS -l app=alloy -o wide

echo ""
echo "3️⃣  Alloy Service 확인"
echo "============================================"
kubectl get svc -n $ALLOY_NS -l app.kubernetes.io/name=alloy 2>/dev/null || kubectl get svc -n $ALLOY_NS -l app=alloy

echo ""
echo "4️⃣  Alloy Ingress 확인"
echo "============================================"
kubectl get ingress -n $ALLOY_NS | grep -i alloy || echo "Ingress를 찾을 수 없습니다."

echo ""
echo "5️⃣  Alloy Pod 로그 (최근 20줄)"
echo "============================================"
POD_NAME=$(kubectl get pods -n $ALLOY_NS -l app.kubernetes.io/name=alloy -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || kubectl get pods -n $ALLOY_NS -l app=alloy -o jsonpath='{.items[0].metadata.name}')

if [ -n "$POD_NAME" ]; then
    echo "Pod: $POD_NAME"
    echo ""
    kubectl logs -n $ALLOY_NS $POD_NAME --tail=20
else
    echo "Pod를 찾을 수 없습니다."
fi

echo ""
echo "============================================"
echo "권장 조치"
echo "============================================"
echo ""

# Pod 상태 확인
POD_STATUS=$(kubectl get pods -n $ALLOY_NS -l app.kubernetes.io/name=alloy -o jsonpath='{.items[0].status.phase}' 2>/dev/null || kubectl get pods -n $ALLOY_NS -l app=alloy -o jsonpath='{.items[0].status.phase}')

if [ "$POD_STATUS" != "Running" ]; then
    echo "❌ Alloy Pod가 Running 상태가 아닙니다: $POD_STATUS"
    echo ""
    echo "조치:"
    echo "  1. Pod 재시작:"
    echo "     kubectl rollout restart deployment -n $ALLOY_NS -l app.kubernetes.io/name=alloy"
    echo ""
    echo "  2. Pod 이벤트 확인:"
    echo "     kubectl describe pod -n $ALLOY_NS $POD_NAME"
else
    echo "✅ Alloy Pod가 Running 상태입니다."
    echo ""
    echo "503 에러가 계속되면:"
    echo "  1. Readiness Probe 확인:"
    echo "     kubectl describe pod -n $ALLOY_NS $POD_NAME | grep -A 10 Readiness"
    echo ""
    echo "  2. Service Endpoints 확인:"
    echo "     kubectl get endpoints -n $ALLOY_NS | grep alloy"
    echo ""
    echo "  3. Alloy 설정 확인:"
    echo "     kubectl logs -n $ALLOY_NS $POD_NAME | grep -i otlp"
fi

echo ""
