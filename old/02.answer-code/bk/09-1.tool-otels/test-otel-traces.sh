#!/bin/bash

echo "=========================================="
echo "OpenTelemetry Traces 테스트"
echo "=========================================="
echo ""

#SERVICE_URL="http://trace-call-app.skala-practice.svc:8080"
SERVICE_URL="https://trace-call-app.skala25a.project.skala-ai.com"

# 1. Pod 상태 확인
echo "1. Pod 상태 확인..."
POD_STATUS=$(kubectl get pods -n skala-practice -l app=trace-call-app -o jsonpath='{.items[0].status.phase}')
if [ "$POD_STATUS" != "Running" ]; then
    echo "✗ Pod가 Running 상태가 아닙니다: $POD_STATUS"
    exit 1
fi
echo "✓ Pod가 정상 실행 중입니다"
echo ""

# 2. OpenTelemetry Agent 로드 확인
echo "2. OpenTelemetry Agent 로드 확인..."
kubectl logs -n skala-practice -l app=trace-call-app --tail=100 | grep -q "opentelemetry-javaagent"
if [ $? -eq 0 ]; then
    echo "✓ OpenTelemetry Java Agent가 정상 로드되었습니다"
else
    echo "⚠ OpenTelemetry Java Agent 로드를 확인할 수 없습니다"
fi
echo ""

# 3. 테스트 요청 보내기
echo "3. 테스트 요청 보내기..."
echo ""

# /api/test/all - 모든 observability 기능 테스트
echo "→ GET /api/test/all (전체 기능 테스트)"
kubectl run -n skala-practice curl-test --image=curlimages/curl:latest --rm -i --restart=Never -- \
  curl -s -w "\nHTTP Status: %{http_code}\n" ${SERVICE_URL}/api/test/all
echo ""

# /api/test/logs - 로그 생성 테스트
echo "→ GET /api/test/logs (로그 생성)"
kubectl run -n skala-practice curl-test --image=curlimages/curl:latest --rm -i --restart=Never -- \
  curl -s -w "\nHTTP Status: %{http_code}\n" ${SERVICE_URL}/api/test/logs
echo ""

# /api/test/metrics - 메트릭 생성 테스트
echo "→ GET /api/test/metrics (메트릭 생성)"
kubectl run -n skala-practice curl-test --image=curlimages/curl:latest --rm -i --restart=Never -- \
  curl -s -w "\nHTTP Status: %{http_code}\n" ${SERVICE_URL}/api/test/metrics
echo ""

# /api/test/traces - 트레이스 생성 테스트
echo "→ GET /api/test/traces (트레이스 생성)"
kubectl run -n skala-practice curl-test --image=curlimages/curl:latest --rm -i --restart=Never -- \
  curl -s -w "\nHTTP Status: %{http_code}\n" ${SERVICE_URL}/api/test/traces
echo ""

# 4. Alloy receiver 로그 확인
echo "4. Alloy receiver에서 traces 수신 확인..."
sleep 3
ALLOY_POD=$(kubectl get pods -n observability -l app.kubernetes.io/name=alloy,app.kubernetes.io/component=alloy-receiver -o jsonpath='{.items[0].metadata.name}')
if [ -n "$ALLOY_POD" ]; then
    echo "Alloy Pod: $ALLOY_POD"
    kubectl logs -n observability $ALLOY_POD -c alloy --since=30s | grep -i "trace" | tail -5
else
    echo "⚠ Alloy receiver pod를 찾을 수 없습니다"
fi
echo ""

# 5. 확인 방법 안내
echo "=========================================="
echo "Grafana에서 확인 방법:"
echo "=========================================="
echo ""
echo "1. Grafana 접속:"
echo "   https://observability.skala25a.project.skala-ai.com"
echo ""
echo "2. Tempo에서 Traces 확인:"
echo "   Explore > Tempo"
echo "   - service.name = trace-call-app"
echo "   - 최근 1시간 데이터 조회"
echo "   - 예상 Operations:"
echo "     * GET /api/test/all"
echo "     * GET /api/test/logs"
echo "     * GET /api/test/metrics"
echo "     * GET /api/test/traces"
echo ""
echo "3. Span 상세 정보 확인:"
echo "   - http.method = GET"
echo "   - http.url = /api/test/..."
echo "   - http.status_code = 200"
echo "   - service.namespace = skala-practice"
echo "   - service.version = 1.0.0"
echo "   - deployment.environment = production"
echo ""
echo "4. Prometheus에서 Metrics 확인:"
echo "   Explore > Prometheus"
echo "   - Metrics: http_server_request_duration_seconds"
echo "   - Labels: {service_name=\"trace-call-app\"}"
echo ""
echo "5. Loki에서 Logs 확인:"
echo "   Explore > Loki"
echo "   - Query: {service_name=\"trace-call-app\"}"
echo "   - Trace ID로 로그-트레이스 연관 확인"
echo ""

echo "=========================================="
echo "테스트 완료!"
echo "=========================================="
