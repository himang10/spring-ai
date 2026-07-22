#!/bin/bash

echo "============================================"
echo "Grafana Alloy 연결 테스트"
echo "============================================"
echo ""

#ALLOY_ENDPOINT="https://alloy.skala25a.project.skala-ai.com"
ALLOY_ENDPOINT="http://localhost:4318"
#ALLOY_ENDPOINT="http://aa53a66e9455e425f9d8e2eba09ff2ac-180318788.ap-northeast-2.elb.amazonaws.com:4318"


# 1. HTTPS 연결 테스트
echo "2️⃣  HTTPS 연결 테스트..."
curl -I "$ALLOY_ENDPOINT" --max-time 5

if [ $? -eq 0 ]; then
    echo "✅ HTTPS 연결 성공"
else
    echo "⚠️  HTTPS 연결 실패 (정상일 수 있음 - Alloy는 루트 경로에 응답하지 않을 수 있음)"
fi

echo ""

# 2. OTLP Traces 엔드포인트 테스트
echo "3️⃣  OTLP Traces 엔드포인트 테스트..."
echo "엔드포인트: ${ALLOY_ENDPOINT}/v1/traces"

# 간단한 OTLP 페이로드 전송 테스트
curl -X POST "${ALLOY_ENDPOINT}/v1/traces" \
  -H "Content-Type: application/json" \
  -d '{"resourceSpans":[]}' \
  --max-time 5 \
  -w "\nHTTP Status: %{http_code}\n"

echo ""

# 3. OTLP Logs 엔드포인트 테스트
echo "4️⃣  OTLP Logs 엔드포인트 테스트..."
echo "엔드포인트: ${ALLOY_ENDPOINT}/v1/logs"

curl -X POST "${ALLOY_ENDPOINT}/v1/logs" \
  -H "Content-Type: application/json" \
  -d '{"resourceLogs":[]}' \
  --max-time 5 \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "============================================"
echo "테스트 완료"
echo "============================================"
echo ""
echo "예상 결과:"
echo "  - HTTP 200 OK: 정상 연결"
echo "  - HTTP 401/403: 인증 필요"
echo "  - HTTP 404: 엔드포인트 경로 확인 필요"
echo "  - HTTP 405: Method Not Allowed (정상 - GET 대신 POST 필요)"
echo "  - 연결 타임아웃: 네트워크 또는 Ingress 설정 확인"
echo ""
echo "Alloy가 정상 작동 중이면 애플리케이션을 실행하세요:"
echo "  ./run-with-alloy.sh"
echo ""
