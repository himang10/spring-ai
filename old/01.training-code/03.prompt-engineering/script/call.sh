#!/bin/bash

# 사용법 출력 함수
show_help() {
    echo "사용법: $0 [API_PATH] [MESSAGE]"
    echo ""
    echo "옵션:"
    echo "  API_PATH  : API 경로 (기본값: /api/chat)"
    echo "  MESSAGE   : 전송할 메시지 (기본값: '너의 이름은 무엇인가요')"
    echo ""
    echo "예제:"
    echo "  $0                                    # 기본값 사용"
    echo "  $0 /api/chat/pt                       # 경로만 변경"
    echo "  $0 /api/chat '안녕하세요'              # 경로와 메시지 변경"
    echo "  $0 - '안녕하세요'                      # 기본 경로 + 메시지 변경"
    exit 0
}

# help 옵션 체크
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
    show_help
fi

# 기본값 설정
DEFAULT_API_PATH="/api/chat"
DEFAULT_MESSAGE="코칭에 대해 설명해줘"

# 파라미터 처리
API_PATH="${1:-$DEFAULT_API_PATH}"
MESSAGE="${2:-$DEFAULT_MESSAGE}"

# '-'를 기본 경로로 처리
if [[ "$API_PATH" == "-" ]]; then
    API_PATH="$DEFAULT_API_PATH"
fi

curl -X POST "http://localhost:8080${API_PATH}" \
-H "Content-Type: application/json" \
-d "{\"message\": \"$MESSAGE\"}"