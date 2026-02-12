#!/bin/bash

echo "============================================"
echo "Langfuse 환경변수 설정 스크립트"
echo "============================================"
echo ""

# Langfuse API 키 입력받기
echo "Langfuse 웹 UI에서 발급받은 API 키를 입력하세요:"
echo "(https://langfuse.skala25a.project.skala-ai.com)"
echo ""

read -p "Public Key (pk-lf-...): " PUBLIC_KEY
read -p "Secret Key (sk-lf-...): " SECRET_KEY

# 입력 확인
if [ -z "$PUBLIC_KEY" ] || [ -z "$SECRET_KEY" ]; then
    echo ""
    echo "❌ API 키가 입력되지 않았습니다."
    exit 1
fi

# Base64 인코딩
AUTH=$(echo -n "$PUBLIC_KEY:$SECRET_KEY" | base64)

echo ""
echo "============================================"
echo "생성된 환경변수 (복사해서 사용하세요)"
echo "============================================"
echo ""
echo "export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel"
echo "export OTEL_EXPORTER_OTLP_HEADERS=\"Authorization=Basic $AUTH\""
echo ""
echo "============================================"
echo ""
echo "다음 명령으로 환경변수를 설정하세요:"
echo ""
echo "  source <(./setup-langfuse.sh)"
echo ""
echo "또는 직접 복사해서 실행:"
echo ""
echo "  export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel"
echo "  export OTEL_EXPORTER_OTLP_HEADERS=\"Authorization=Basic $AUTH\""
echo ""
echo "영구 설정 (~/.zshrc 또는 ~/.bashrc에 추가):"
echo ""
echo "  echo 'export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel' >> ~/.zshrc"
echo "  echo 'export OTEL_EXPORTER_OTLP_HEADERS=\"Authorization=Basic $AUTH\"' >> ~/.zshrc"
echo "  source ~/.zshrc"
echo ""
