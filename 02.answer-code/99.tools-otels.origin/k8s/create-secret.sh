#!/bin/bash

echo "============================================"
echo "Kubernetes Secret 생성 스크립트"
echo "============================================"
echo ""

# Namespace 입력
read -p "Namespace (기본값: default): " NAMESPACE
NAMESPACE=${NAMESPACE:-default}

echo ""
echo "API 키를 입력하세요:"
echo ""

# OpenAI API Key
read -p "OpenAI API Key (sk-...): " OPENAI_KEY
if [ -z "$OPENAI_KEY" ]; then
    echo "❌ OpenAI API Key는 필수입니다."
    exit 1
fi

# Weather API Key
read -p "Weather API Key: " WEATHER_KEY
if [ -z "$WEATHER_KEY" ]; then
    echo "⚠️  Weather API Key가 없습니다. 날씨 기능을 사용하지 않습니다."
    WEATHER_KEY="not-configured"
fi

echo ""
echo "Langfuse API 키를 입력하세요 (선택사항):"
echo "(Langfuse를 사용하지 않으려면 Enter)"
echo ""

# Langfuse Public Key
read -p "Langfuse Public Key (pk-lf-...): " LANGFUSE_PUBLIC
# Langfuse Secret Key
read -p "Langfuse Secret Key (sk-lf-...): " LANGFUSE_SECRET

# Langfuse 설정
if [ -n "$LANGFUSE_PUBLIC" ] && [ -n "$LANGFUSE_SECRET" ]; then
    LANGFUSE_ENABLED=true
    LANGFUSE_AUTH=$(echo -n "$LANGFUSE_PUBLIC:$LANGFUSE_SECRET" | base64)
    LANGFUSE_ENDPOINT="https://langfuse.skala25a.project.skala-ai.com/api/public/otel"
    LANGFUSE_HEADERS="Authorization=Basic $LANGFUSE_AUTH"
    echo ""
    echo "✅ Langfuse 설정이 활성화됩니다."
else
    LANGFUSE_ENABLED=false
    LANGFUSE_ENDPOINT="http://localhost:4318/v1/traces"
    LANGFUSE_HEADERS=""
    echo ""
    echo "⚠️  Langfuse 설정을 건너뜁니다."
fi

echo ""
echo "============================================"
echo "Secret 생성 중..."
echo "============================================"
echo ""

# Secret 생성
kubectl create secret generic spring-ai-secrets \
  --from-literal=OPEN_AI_KEY="$OPENAI_KEY" \
  --from-literal=WEATHER_API_KEY="$WEATHER_KEY" \
  --from-literal=OTEL_EXPORTER_OTLP_ENDPOINT="$LANGFUSE_ENDPOINT" \
  --from-literal=OTEL_EXPORTER_OTLP_HEADERS="$LANGFUSE_HEADERS" \
  --namespace=$NAMESPACE \
  --dry-run=client -o yaml | kubectl apply -f -

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Secret 'spring-ai-secrets'가 생성되었습니다."
    echo ""
    echo "다음 명령으로 확인할 수 있습니다:"
    echo "  kubectl get secret spring-ai-secrets -n $NAMESPACE"
    echo "  kubectl describe secret spring-ai-secrets -n $NAMESPACE"
    echo ""
    echo "이제 애플리케이션을 배포할 수 있습니다:"
    echo "  kubectl apply -f k8s/deployment.yaml"
    echo "  kubectl apply -f k8s/service.yaml"
    echo "  kubectl apply -f k8s/ingress.yaml"
else
    echo ""
    echo "❌ Secret 생성 실패"
    exit 1
fi
