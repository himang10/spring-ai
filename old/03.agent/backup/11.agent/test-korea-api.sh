#!/bin/bash

# 한국관광공사 API 테스트 스크립트

# 환경변수에서 API 키 가져오기
if [ -z "$KOREA_TOUR_API_KEY" ]; then
    echo "❌ 환경변수 KOREA_TOUR_API_KEY가 설정되지 않았습니다."
    echo ""
    echo "사용 방법:"
    echo "  export KOREA_TOUR_API_KEY='your_decoding_key_here'"
    echo "  ./test-korea-api.sh"
    echo ""
    echo "또는 직접 실행:"
    echo "  KOREA_TOUR_API_KEY='your_key' ./test-korea-api.sh"
    echo ""
    exit 1
fi

API_KEY="$KOREA_TOUR_API_KEY"
ENDPOINT="http://apis.data.go.kr/B551011/KorService1"
KEYWORD="제주도"

echo "======================================"
echo "한국관광공사 API 테스트"
echo "======================================"
echo "API 키 (앞 20자): ${API_KEY:0:20}..."
echo "검색어: $KEYWORD"
echo ""

# URL 인코딩된 키워드
ENCODED_KEYWORD=$(echo -n "$KEYWORD" | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g')

# 전체 URL 생성
URL="${ENDPOINT}/searchKeyword1?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=TravelAgent&_type=json&keyword=${ENCODED_KEYWORD}&numOfRows=10&pageNo=1&listYN=Y&arrange=A"

echo "요청 URL:"
echo "$URL"
echo ""
echo "======================================"
echo "응답:"
echo "======================================"

# API 호출
curl -s -w "\n\nHTTP 상태 코드: %{http_code}\n" "$URL" | jq '.' 2>/dev/null || curl -s -w "\n\nHTTP 상태 코드: %{http_code}\n" "$URL"

echo ""
echo "======================================"
echo ""
echo "💡 팁:"
echo "1. HTTP 상태 코드가 200이면 정상"
echo "2. 500 에러가 나오면 API 키 확인 필요"
echo "3. resultCode가 '0000'이면 성공"
echo "4. resultCode가 다른 값이면 resultMsg 확인"
echo ""
echo "API 키 확인 방법:"
echo "1. https://www.data.go.kr/ 로그인"
echo "2. 마이페이지 → 오픈API → 개발계정"
echo "3. '한국관광공사_국문 관광정보 서비스' 클릭"
echo "4. '일반 인증키(Decoding)' 값을 사용하세요 ⭐"
