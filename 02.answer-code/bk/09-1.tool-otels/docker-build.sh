NAME=spring-ai
IMAGE_NAME="tools"
VERSION="1.0.0"
DOCKER_REGISTRY="amdp-registry.skala-ai.com/skala25a"

CPU_PLATFORM=amd64
IS_CACHE="--no-cache"

# Docker 이미지 빌드
docker build \
  --tag ${DOCKER_REGISTRY}/${NAME}-${IMAGE_NAME}:${VERSION} \
  --file Dockerfile \
  --platform linux/${CPU_PLATFORM} \
  ${IS_CACHE} .
