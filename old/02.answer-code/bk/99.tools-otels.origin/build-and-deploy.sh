#!/bin/bash

set -e

echo "============================================"
echo "Spring AI Tools - Build & Deploy"
echo "============================================"
echo ""

# 설정
IMAGE_NAME="spring-ai-tools"
IMAGE_TAG="${1:-latest}"
NAMESPACE="${2:-default}"
REGISTRY="${3:-}"  # 빈 값이면 로컬 이미지만 사용

if [ -n "$REGISTRY" ]; then
    FULL_IMAGE="$REGISTRY/$IMAGE_NAME:$IMAGE_TAG"
else
    FULL_IMAGE="$IMAGE_NAME:$IMAGE_TAG"
fi

echo "이미지: $FULL_IMAGE"
echo "네임스페이스: $NAMESPACE"
echo ""

# 1. 애플리케이션 빌드
echo "1️⃣  Maven 빌드 중..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Maven 빌드 실패"
    exit 1
fi

echo "✅ Maven 빌드 완료"
echo ""

# 2. Docker 이미지 빌드
echo "2️⃣  Docker 이미지 빌드 중..."
docker build -t $FULL_IMAGE .

if [ $? -ne 0 ]; then
    echo "❌ Docker 빌드 실패"
    exit 1
fi

echo "✅ Docker 이미지 빌드 완료: $FULL_IMAGE"
echo ""

# 3. Docker Registry에 푸시 (옵션)
if [ -n "$REGISTRY" ]; then
    echo "3️⃣  Docker Registry에 푸시 중..."
    docker push $FULL_IMAGE

    if [ $? -ne 0 ]; then
        echo "❌ Docker 푸시 실패"
        exit 1
    fi

    echo "✅ Docker 이미지 푸시 완료"
    echo ""
else
    echo "3️⃣  Registry 푸시 건너뜀 (로컬 이미지 사용)"
    echo ""
fi

# 4. Secret 확인
echo "4️⃣  Secret 확인 중..."
kubectl get secret spring-ai-secrets -n $NAMESPACE > /dev/null 2>&1

if [ $? -ne 0 ]; then
    echo "⚠️  Secret 'spring-ai-secrets'가 없습니다."
    echo ""
    read -p "Secret을 생성하시겠습니까? (y/n): " CREATE_SECRET

    if [ "$CREATE_SECRET" = "y" ] || [ "$CREATE_SECRET" = "Y" ]; then
        cd k8s
        ./create-secret.sh
        cd ..
    else
        echo "❌ Secret이 필요합니다. 배포를 중단합니다."
        exit 1
    fi
else
    echo "✅ Secret 'spring-ai-secrets' 확인 완료"
fi

echo ""

# 5. Deployment YAML 업데이트 (이미지 경로)
echo "5️⃣  Deployment 설정 업데이트 중..."
sed -i.bak "s|image:.*|image: $FULL_IMAGE|g" k8s/deployment.yaml
rm k8s/deployment.yaml.bak 2>/dev/null || true

echo "✅ Deployment 설정 업데이트 완료"
echo ""

# 6. Kubernetes 배포
echo "6️⃣  Kubernetes에 배포 중..."
kubectl apply -f k8s/deployment.yaml -n $NAMESPACE
kubectl apply -f k8s/service.yaml -n $NAMESPACE

if [ $? -ne 0 ]; then
    echo "❌ Kubernetes 배포 실패"
    exit 1
fi

echo "✅ Kubernetes 배포 완료"
echo ""

# 7. Ingress 배포 (선택)
if [ -f k8s/ingress.yaml ]; then
    read -p "Ingress를 배포하시겠습니까? (y/n): " DEPLOY_INGRESS

    if [ "$DEPLOY_INGRESS" = "y" ] || [ "$DEPLOY_INGRESS" = "Y" ]; then
        kubectl apply -f k8s/ingress.yaml -n $NAMESPACE
        echo "✅ Ingress 배포 완료"
    else
        echo "⏭️  Ingress 배포 건너뜀"
    fi
fi

echo ""

# 8. 배포 상태 확인
echo "7️⃣  배포 상태 확인 중..."
echo ""
kubectl rollout status deployment/spring-ai-tools -n $NAMESPACE

echo ""
echo "============================================"
echo "🎉 배포 완료!"
echo "============================================"
echo ""
echo "배포 정보:"
echo "  - 이미지: $FULL_IMAGE"
echo "  - 네임스페이스: $NAMESPACE"
echo ""
echo "다음 명령으로 확인할 수 있습니다:"
echo "  kubectl get pods -n $NAMESPACE -l app=spring-ai-tools"
echo "  kubectl logs -f -n $NAMESPACE -l app=spring-ai-tools"
echo "  kubectl get svc -n $NAMESPACE spring-ai-tools"
echo ""
echo "포트 포워딩으로 로컬에서 테스트:"
echo "  kubectl port-forward -n $NAMESPACE svc/spring-ai-tools 8080:80"
echo "  curl http://localhost:8080/actuator/health"
echo ""
