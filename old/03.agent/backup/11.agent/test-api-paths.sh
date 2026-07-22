#!/bin/bash

# 한국관광공사 API 테스트 - KorService2 다양한 경로 테스트

API_KEY="6a012aa16f4de7525628f7eb87abfb4b349861f1708bd088a1a0b33ec5827b7e"
KEYWORD="제주도"

echo "======================================"
echo "한국관광공사 API 경로 테스트"
echo "======================================"
echo ""

# 테스트 1: KorService2 + searchKeyword (버전번호 없음)
echo "[1] KorService2/searchKeyword"
curl -s "https://apis.data.go.kr/B551011/KorService2/searchKeyword?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=Test&_type=json&keyword=${KEYWORD}&numOfRows=1" \
  | python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"resultCode: {data.get('response',{}).get('header',{}).get('resultCode','ERROR')}\")" 2>/dev/null || echo "ERROR"

# 테스트 2: KorService2 + searchKeyword1 (버전번호 포함)
echo "[2] KorService2/searchKeyword1"
curl -s "https://apis.data.go.kr/B551011/KorService2/searchKeyword1?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=Test&_type=json&keyword=${KEYWORD}&numOfRows=1" \
  | python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"resultCode: {data.get('response',{}).get('header',{}).get('resultCode','ERROR')}\")" 2>/dev/null || echo "ERROR"

# 테스트 3: KorService1 + searchKeyword1 (기존 방식)
echo "[3] KorService1/searchKeyword1"
curl -s "http://apis.data.go.kr/B551011/KorService1/searchKeyword1?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=Test&_type=json&keyword=${KEYWORD}&numOfRows=1" \
  | python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"resultCode: {data.get('response',{}).get('header',{}).get('resultCode','ERROR')}\")" 2>/dev/null || echo "ERROR"

# 테스트 4: KorService1 + searchKeyword (버전번호 없음)
echo "[4] KorService1/searchKeyword"
curl -s "http://apis.data.go.kr/B551011/KorService1/searchKeyword?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=Test&_type=json&keyword=${KEYWORD}&numOfRows=1" \
  | python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"resultCode: {data.get('response',{}).get('header',{}).get('resultCode','ERROR')}\")" 2>/dev/null || echo "ERROR"

echo ""
echo "======================================"
echo "💡 resultCode가 '0000'이면 성공!"
echo "======================================"
