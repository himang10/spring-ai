#!/bin/bash

# OpenAI API Key 확인
if [ -z "$OPEN_AI_KEY" ]; then
    echo "❌ OPEN_AI_KEY 환경변수가 설정되지 않았습니다."
    echo "다음 명령으로 설정해주세요:"
    echo "  export OPEN_AI_KEY=your-openai-api-key"
    exit 1
fi

echo "✅ OpenAI API Key 확인 완료"

# Langfuse 환경변수 확인 (선택사항)
if [ -z "$OTEL_EXPORTER_OTLP_ENDPOINT" ]; then
    echo ""
    echo "⚠️  Langfuse 환경변수가 설정되지 않았습니다."
    echo ""
    echo "Langfuse를 사용하려면 다음과 같이 설정하세요:"
    echo ""
    echo "1. API 키를 Base64로 인코딩:"
    echo "   AUTH=\$(echo -n 'pk-lf-YOUR_PUBLIC:sk-lf-YOUR_SECRET' | base64)"
    echo ""
    echo "2. 환경변수 설정:"
    echo "   export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel"
    echo "   export OTEL_EXPORTER_OTLP_HEADERS=\"Authorization=Basic \$AUTH\""
    echo ""
    echo "또는 setup-langfuse.sh 스크립트를 사용하세요:"
    echo "   ./setup-langfuse.sh"
    echo ""
    echo "Langfuse 없이 계속 실행합니다..."
else
    echo "✅ Langfuse (OpenTelemetry) 환경변수 설정 완료"
fi

echo ""
echo "============================================"
echo "Spring AI Tools 데모를 시작합니다."
echo "============================================"
echo ""

# Spring Boot 애플리케이션 실행
mvn spring-boot:run
