#!/bin/bash

# Ollama Docker 이미지 다운로드 및 실행
docker run -d \
  --name ollama \
  -p 11434:11434 \
  -v ollama-model:/root/.ollama \
  ollama/ollama

# 실행 확인
docker ps | grep ollama
