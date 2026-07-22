#!/bin/bash

# run.sh로 띄운 네이티브 Ollama 프로세스와 OpenWebUI 컨테이너를 종료한다.

WEBUI_CONTAINER_NAME="open-webui"

echo "OpenWebUI 컨테이너를 종료합니다..."
docker stop "$WEBUI_CONTAINER_NAME" 2>/dev/null

echo "Ollama 프로세스를 종료합니다..."
pkill -x ollama 2>/dev/null

echo "종료되었습니다."
