#!/bin/bash

# OpenAI API Key 확인
if [ -z "$OPEN_AI_KEY" ]; then
    echo "❌ OPEN_AI_KEY 환경변수가 설정되지 않았습니다."
    echo "다음 명령으로 설정해주세요:"
    echo "  export OPEN_AI_KEY=your-openai-api-key"
    exit 1
fi

echo "✅ OpenAI API Key 확인 완료"
echo ""
echo "============================================"
echo "Spring AI Advisors 데모를 시작합니다."
echo "============================================"
echo ""

# Spring Boot 애플리케이션 실행
mvn spring-boot:run
