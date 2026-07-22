#!/bin/bash

# 한국관광공사 API 테스트 스크립트 (KorService2)

API_KEY="6a012aa16f4de7525628f7eb87abfb4b349861f1708bd088a1a0b33ec5827b7e"
BASE_URL="https://apis.data.go.kr/B551011/KorService2"

echo "======================================"
echo "한국관광공사 API 테스트 (KorService2)"
echo "======================================"
echo ""

# 테스트 1: searchKeyword API (버전 번호 제거)
echo "테스트 1: /searchKeyword API"
echo "--------------------------------------"
KEYWORD="제주도"
URL="${BASE_URL}/searchKeyword?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=TestApp&_type=json&keyword=${KEYWORD}&numOfRows=3"

echo "요청 URL:"
echo "$URL"
echo ""
echo "응답:"
curl -s "$URL" | python3 -m json.tool 2>/dev/null || curl -s "$URL"

echo ""
echo ""

# 테스트 2: detailCommon API (버전 번호 제거) - 제주 월드컵경기장 (contentId: 126508)
echo "테스트 2: /detailCommon API"
echo "--------------------------------------"
CONTENT_ID="126508"
URL="${BASE_URL}/detailCommon?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=TestApp&_type=json&contentId=${CONTENT_ID}&defaultYN=Y&overviewYN=Y"

echo "요청 URL:"
echo "$URL"
echo ""
echo "응답:"
curl -s "$URL" | python3 -m json.tool 2>/dev/null || curl -s "$URL"

echo ""
echo "======================================"
echo "테스트 완료"
echo "======================================"
