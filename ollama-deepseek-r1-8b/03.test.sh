#!/bin/bash

# Ollama가 정상적으로 동작하는지 확인하는 테스트 스크립트
#
# 사용법:
#   ./03.test.sh
#   BASE_URL=http://localhost:11434 CHAT_MODEL=deepseek-r1:8b EMBED_MODEL=mxbai-embed-large ./03.test.sh

BASE_URL="${BASE_URL:-http://localhost:11434}"
CHAT_MODEL="${CHAT_MODEL:-deepseek-r1:8b}"
EMBED_MODEL="${EMBED_MODEL:-mxbai-embed-large}"

# jq가 있으면 예쁘게 출력하고, 없으면 원본 그대로 출력한다.
pretty() {
  if command -v jq > /dev/null 2>&1; then
    jq .
  else
    cat
  fi
}

echo "============================================================"
echo "1) 서버 상태 확인 (${BASE_URL}/api/tags)"
echo "============================================================"
if ! curl -sf "${BASE_URL}/api/tags" | pretty; then
  echo "Ollama 서버(${BASE_URL})에 접속할 수 없습니다. 컨테이너가 실행 중인지 확인하세요."
  exit 1
fi

echo
echo "============================================================"
echo "2) Chat 모델(${CHAT_MODEL}) 응답 테스트"
echo "============================================================"
curl -s "${BASE_URL}/api/generate" -d "{
  \"model\": \"${CHAT_MODEL}\",
  \"prompt\": \"1 + 1은 얼마야? 숫자로만 답해줘.\",
  \"stream\": false
}" | pretty

echo
echo "============================================================"
echo "3) Embedding 모델(${EMBED_MODEL}) 응답 테스트"
echo "============================================================"
curl -s "${BASE_URL}/api/embeddings" -d "{
  \"model\": \"${EMBED_MODEL}\",
  \"prompt\": \"This is a test sentence\"
}" | pretty

echo
echo "테스트가 완료되었습니다."
