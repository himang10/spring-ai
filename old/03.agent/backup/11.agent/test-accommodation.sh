#!/bin/bash

# searchKeyword2 + contentTypeId=32 (숙박) 테스트
API_KEY="6a012aa16f4de7525628f7eb87abfb4b349861f1708bd088a1a0b33ec5827b7e"
KEYWORD=$(echo "제주도" | iconv -t UTF-8 | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g')

echo "=== searchKeyword2 + contentTypeId=32 (숙박) 테스트 ==="
echo "키워드: 제주도"
echo ""

curl -s "https://apis.data.go.kr/B551011/KorService2/searchKeyword2?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=AppTest&_type=json&keyword=${KEYWORD}&contentTypeId=32&numOfRows=5&pageNo=1&arrange=B" | jq '.response | {header, body: {totalCount: .body.totalCount, numOfRows: .body.numOfRows}}'

echo ""
echo "=== 첫 번째 결과 ==="
curl -s "https://apis.data.go.kr/B551011/KorService2/searchKeyword2?serviceKey=${API_KEY}&MobileOS=ETC&MobileApp=AppTest&_type=json&keyword=${KEYWORD}&contentTypeId=32&numOfRows=5&pageNo=1&arrange=B" | jq '.response.body.items.item[0] | {title, addr1, tel, contenttypeid}'
