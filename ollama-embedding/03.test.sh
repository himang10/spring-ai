#!/bin/bash

# Embedding 생성 테스트
curl http://localhost:11434/api/embeddings -d '{
  "model": "mxbai-embed-large",
  "prompt": "This is a test sentence"
}'
