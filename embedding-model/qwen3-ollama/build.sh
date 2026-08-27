#!/bin/bash

# Dockerfile을 기준으로 qwen3-embedding 모델이 포함된 Ollama 이미지를 빌드하는 스크립트
#
# 사용법:
#   ./build.sh
#   IMAGE_NAME=my-ollama-qwen3-embedding EMBED_MODEL=qwen3-embedding ./build.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

IMAGE_NAME="${IMAGE_NAME:-ollama-qwen3-embedding}"
IMAGE_TAG="${IMAGE_TAG:-1.0}"
EMBED_MODEL="${EMBED_MODEL:-qwen3-embedding}"

echo "이미지(${IMAGE_NAME}:${IMAGE_TAG})를 빌드합니다. (모델: ${EMBED_MODEL})"

docker build \
  --build-arg OLLAMA_MODEL="${EMBED_MODEL}" \
  -t "${IMAGE_NAME}:${IMAGE_TAG}" \
  -f "${SCRIPT_DIR}/Dockerfile" \
  "${SCRIPT_DIR}"

echo "이미지 빌드가 완료되었습니다: ${IMAGE_NAME}:${IMAGE_TAG}"
docker images "${IMAGE_NAME}"
