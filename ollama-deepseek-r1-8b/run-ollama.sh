#!/bin/bash

# build.sh로 빌드한 Ollama 이미지를 컨테이너로 실행하고 OpenWebUI 컨테이너도 함께 실행한다.
# OpenWebUI는 38080 포트로 노출하고, 실행 후 두 컨테이너를 브라우저로 연다.

set -e

IMAGE_NAME="ollama-deepseek-r1-8b:1.0"
CONTAINER_NAME="ollama"
NETWORK_NAME="ollama-net"

WEBUI_IMAGE="ghcr.io/open-webui/open-webui:main"
WEBUI_CONTAINER_NAME="open-webui"
WEBUI_PORT=38080

docker network inspect "$NETWORK_NAME" > /dev/null 2>&1 || docker network create "$NETWORK_NAME"

if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
  docker start "$CONTAINER_NAME"
else
  docker run -d \
    --name "$CONTAINER_NAME" \
    --network "$NETWORK_NAME" \
    -p 11434:11434 \
    -v ollama-model:/root/.ollama \
    "$IMAGE_NAME"
fi

if docker ps -a --format '{{.Names}}' | grep -qx "$WEBUI_CONTAINER_NAME"; then
  docker start "$WEBUI_CONTAINER_NAME"
else
  docker run -d \
    --name "$WEBUI_CONTAINER_NAME" \
    --network "$NETWORK_NAME" \
    -p "$WEBUI_PORT:8080" \
    -e OLLAMA_BASE_URL="http://$CONTAINER_NAME:11434" \
    -v open-webui:/app/backend/data \
    "$WEBUI_IMAGE"
fi

open "http://localhost:$WEBUI_PORT"
open "http://localhost:11434"
