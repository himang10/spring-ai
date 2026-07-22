#!/bin/bash

# searchStay2 API 테스트
API_KEY="6a012aa16f4de7525628f7eb87abfb4b349861f1708bd088a1a0b33ec5827b7e"
KEYWORD=$(echo "제주도 숙소" | iconv -t UTF-8 | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g')

echo "=== searchStay2 API 테스트 ==="
echo "키워드: 제주도 숙소"
echo ""

curl -s "https://apis.data.go.kr/B551011/KorService2/searchStay2?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=AppTest&_type=json&keyword=${KEYWORD}&numOfRows=5&pageNo=1&arrange=B" | jq '.'

echo ""
echo "=== 응답 구조 확인 ==="
curl -s "https://apis.data.go.kr/B551011/KorService2/searchStay2?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=AppTest&_type=json&keyword=${KEYWORD}&numOfRows=5&pageNo=1&arrange=B" | jq '.response.header'
