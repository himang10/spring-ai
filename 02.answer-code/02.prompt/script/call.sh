#!/bin/bash

# 기본 경로 설정
API_PATH="${1:-/api/chat}"

curl -X POST "http://localhost:8080${API_PATH}" \
-H "Content-Type: application/json" \
-d '{"message": "너의 이름은 무엇인가요"}'