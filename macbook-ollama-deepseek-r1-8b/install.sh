#!/bin/bash

# macOS(Apple Silicon)에서 Ollama를 네이티브로 설치하고 deepseek-r1:8b 모델을 받는다.
# Docker 컨테이너 대신 네이티브로 실행하면 Metal GPU 가속을 사용할 수 있어 훨씬 빠르다.

set -e

if ! command -v brew > /dev/null 2>&1; then
  echo "Homebrew가 설치되어 있지 않습니다. https://brew.sh 에서 먼저 설치해주세요."
  exit 1
fi

if ! command -v ollama > /dev/null 2>&1; then
  echo "Ollama를 설치합니다..."
  brew install ollama
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

# echo "deepseek-r1:8b 모델을 다운로드합니다..."
# ollama pull deepseek-r1:8b
echo "qwen3:4b-instruct 모델을 다운로드합니다..."
ollama pull qwen3:4b-instruct
echo "설치가 완료되었습니다. ./run.sh 로 실행하세요."
