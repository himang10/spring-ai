#!/bin/bash

echo "============================================"
echo "Spring AI + Grafana Alloy (OTEL Collector)"
echo "============================================"
echo ""

# .env.alloy 파일이 있으면 자동 로드
if [ -f .env.alloy ]; then
    echo "📁 .env.alloy 파일 발견 - 환경변수 로드 중..."
    echo ""

    # .env.alloy 파일에서 환경변수 로드
    # 주석(#)과 빈 줄 제외하고 직접 export
    while IFS= read -r line; do
        # 주석이나 빈 줄이 아니고, 환경변수 형식인 경우만 export
        if [[ "$line" =~ ^[A-Za-z_][A-Za-z0-9_]*= ]]; then
            export "$line"
        fi
    done < .env.alloy

    echo "✅ .env.alloy 파일에서 환경변수 로드 완료"
    echo ""
else
    echo "ℹ️  .env.alloy 파일이 없습니다."
    echo "   템플릿을 사용하려면:"
    echo "   cp .env.alloy.example .env.alloy"
    echo ""
fi

# OpenAI API Key 확인
if [ -z "$OPEN_AI_KEY" ]; then
    echo "❌ OPEN_AI_KEY 환경변수가 설정되지 않았습니다."
    echo ""
    echo "다음 중 하나를 선택하세요:"
    echo ""
    echo "방법 1) 직접 export:"
    echo "  export OPEN_AI_KEY=your-openai-api-key"
    echo ""
    echo "방법 2) .env.alloy 파일 사용:"
    echo "  cp .env.alloy.example .env.alloy"
    echo "  vim .env.alloy  # OPEN_AI_KEY 값 입력"
    echo "  ./run-with-alloy.sh"
    echo ""
    exit 1
fi

if [ -z "$WEATHER_API_KEY" ]; then
    echo "❌ WEATHER_API_KEY 환경변수가 설정되지 않았습니다."
    echo ""
    echo "다음 중 하나를 선택하세요:"
    echo ""
    echo "방법 1) 직접 export:"
    echo "  export WEATHER_API_KEY=your-weather-api-key"
    echo ""
    echo "방법 2) .env.alloy 파일 사용:"
    echo "  cp .env.alloy.example .env.alloy"
    echo "  vim .env.alloy  # WEATHER_API_KEY 값 입력"
    echo "  ./run-with-alloy.sh"
    echo ""
    exit 1
fi

echo "============================================"
echo "OpenTelemetry 설정"
echo "============================================"
echo "OTLP Collector: Grafana Alloy"
echo "Endpoint: $OTEL_EXPORTER_OTLP_ENDPOINT"
echo ""
echo "설정된 환경변수:"
echo "  OTEL_EXPORTER_OTLP_ENDPOINT=$OTEL_EXPORTER_OTLP_ENDPOINT"
echo "  OTEL_EXPORTER_OTLP_PROTOCOL=$OTEL_EXPORTER_OTLP_PROTOCOL"
echo "  OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=$OTEL_EXPORTER_OTLP_TRACES_ENDPOINT"
echo "  OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=$OTEL_EXPORTER_OTLP_LOGS_ENDPOINT"
echo "  OTEL_SERVICE_NAME=$OTEL_SERVICE_NAME"
echo ""


echo ""
echo "============================================"
echo "애플리케이션 시작"
echo "============================================"
echo ""

# Spring Boot 애플리케이션 실행
mvn spring-boot:run
