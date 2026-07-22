#!/bin/bash

# 컨테이너 내부에서 순차적으로 명령 실행
# embedding model pull (여러 옵션 중 선택)
# ollama pull nomic-embed-text        # 추천: 137M, 영어 특화
# ollama pull all-minilm              # 23M, 경량 모델
#

docker exec -i ollama bash << 'EOF'
ollama pull mxbai-embed-large
ollama list
exit
EOF

echo "모델 설치 및 확인이 완료되었습니다."
