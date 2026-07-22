#!/bin/bash

# 네이티브 Ollama(deepseek-r1:8b, Metal GPU 가속)를 실행하고
# OpenWebUI(Docker)를 함께 띄운다.
# OpenWebUI는 38080 포트로 노출하고, 실행 후 브라우저로 연다.

set -e

WEBUI_IMAGE="ghcr.io/open-webui/open-webui:main"
WEBUI_CONTAINER_NAME="open-webui"
WEBUI_PORT=38080

if ! docker info > /dev/null 2>&1; then
  echo "Docker가 실행 중이 아닙니다. Docker Desktop을 먼저 실행해주세요."
  exit 1
fi

if curl -sf http://localhost:11434/api/tags > /dev/null 2>&1; then
  echo "Ollama가 이미 실행 중입니다."
else
  echo "Ollama를 백그라운드로 시작합니다..."
  OLLAMA_HOST=0.0.0.0 OLLAMA_CONTEXT_LENGTH=8192 nohup ollama serve > /tmp/ollama.log 2>&1 &

  echo "Ollama 서버가 준비될 때까지 대기합니다..."
  for i in $(seq 1 30); do
    curl -sf http://localhost:11434/api/tags > /dev/null 2>&1 && break
    sleep 1
  done

  if ! curl -sf http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "Ollama 서버가 시작되지 않았습니다. /tmp/ollama.log 를 확인하세요."
    exit 1
  fi
fi

# 기존 컨테이너는 제거하고 항상 최신 설정으로 재생성한다.
# (채팅 기록/계정 등 데이터는 open-webui volume에 있어 유지된다)
if docker ps -a --format '{{.Names}}' | grep -qx "$WEBUI_CONTAINER_NAME"; then
  echo "기존 OpenWebUI 컨테이너를 재생성합니다..."
  docker rm -f "$WEBUI_CONTAINER_NAME" > /dev/null
fi

# ENABLE_PERSISTENT_CONFIG=False: OpenWebUI가 내부 DB에 저장된 설정 대신
# 환경변수(OLLAMA_BASE_URL)를 항상 우선하도록 한다.
docker run -d \
  --name "$WEBUI_CONTAINER_NAME" \
  -p "$WEBUI_PORT:8080" \
  -e OLLAMA_BASE_URL="http://host.docker.internal:11434" \
  -e ENABLE_PERSISTENT_CONFIG=False \
  -v open-webui:/app/backend/data \
  --restart unless-stopped \
  "$WEBUI_IMAGE" > /dev/null

echo "OpenWebUI가 준비될 때까지 대기합니다..."
for i in $(seq 1 60); do
  curl -sf "http://localhost:$WEBUI_PORT/health" > /dev/null 2>&1 && break
  sleep 2
done

if ! curl -sf "http://localhost:$WEBUI_PORT/health" > /dev/null 2>&1; then
  echo "OpenWebUI가 시작되지 않았습니다. 'docker logs $WEBUI_CONTAINER_NAME' 을 확인하세요."
  exit 1
fi

echo "Ollama가 http://localhost:11434 에서 네이티브로 대기 중입니다."
echo "OpenWebUI가 http://localhost:$WEBUI_PORT 에서 대기 중입니다."
open "http://localhost:$WEBUI_PORT"
