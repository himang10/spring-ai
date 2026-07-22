#!/bin/bash

# Dockerfile을 기준으로 deepseek-r1:8b 모델이 포함된 Ollama 이미지를 빌드하는 스크립트
#
# 사용법:
#   ./build.sh
#   IMAGE_NAME=my-ollama-deepseek-r1-8b CHAT_MODEL=deepseek-r1:8b ./build.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

IMAGE_NAME="${IMAGE_NAME:-ollama-deepseek-r1-8b}"
IMAGE_TAG="${IMAGE_TAG:-1.0}"
#CHAT_MODEL="${CHAT_MODEL:-deepseek-r1:8b}"
CHAT_MODEL="${CHAT_MODEL:-qwen3:4b-instruct}"

echo "이미지(${IMAGE_NAME}:${IMAGE_TAG})를 빌드합니다. (모델: ${CHAT_MODEL})"

docker build \
  --build-arg OLLAMA_MODEL="${CHAT_MODEL}" \
  -t "${IMAGE_NAME}:${IMAGE_TAG}" \
  -f "${SCRIPT_DIR}/Dockerfile" \
  "${SCRIPT_DIR}"

echo "이미지 빌드가 완료되었습니다: ${IMAGE_NAME}:${IMAGE_TAG}"
docker images "${IMAGE_NAME}"
