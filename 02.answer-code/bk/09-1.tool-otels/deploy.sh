#!/bin/bash

set -e

echo "=========================================="
echo "OpenTelemetry Java Agent 애플리케이션 배포"
echo "=========================================="
echo ""

# 1. Maven 빌드
echo "1. Maven 빌드 시작..."
./mvnw clean package -DskipTests
echo "✓ Maven 빌드 완료"
echo ""

# 2. OpenTelemetry Agent 다운로드 확인
if [ ! -f "target/opentelemetry-javaagent.jar" ]; then
    echo "✗ OpenTelemetry Java Agent가 다운로드되지 않았습니다."
    exit 1
fi
echo "✓ OpenTelemetry Java Agent 확인됨"
echo ""

# 3. Docker 이미지 빌드
echo "2. Docker 이미지 빌드 시작..."
./docker-build.sh
echo "✓ Docker 이미지 빌드 완료"
echo ""

# 4. Docker 이미지 푸시
echo "3. Docker 이미지 푸시 시작..."
./docker-push.sh
echo "✓ Docker 이미지 푸시 완료"
echo ""

# 5. Kubernetes 배포
echo "4. Kubernetes 배포 시작..."
kubectl apply -f k8s/deploy.yaml
kubectl apply -f k8s/service.yaml
echo "✓ Kubernetes 리소스 배포 완료"
echo ""

# 7. 배포 상태 확인
echo "5. 배포 상태 확인 중..."
kubectl rollout restart deployment/trace-call-app -n skala-practice
kubectl rollout status deployment/trace-call-app -n skala-practice
echo ""

# 8. Pod 목록 출력
echo "=========================================="
echo "배포된 Pod 목록:"
echo "=========================================="
kubectl get pods -n skala-practice -l app=trace-call-app
echo ""

# 9. Service 정보 출력
echo "=========================================="
echo "Service 정보:"
echo "=========================================="
kubectl get svc -n skala-practice trace-call-app
echo ""

# 10. 사용 가능한 엔드포인트
echo "=========================================="
echo "사용 가능한 엔드포인트:"
echo "=========================================="
echo "내부 Service: http://trace-call-app.skala-practice.svc:8080"
echo "Ingress (TLS): https://trace-call-app.skala25a.project.skala-ai.com"
echo ""

# 11. 테스트 명령어
echo "=========================================="
echo "테스트 명령어:"
echo "=========================================="
echo "# Pod 로그 확인 (OpenTelemetry 초기화 확인)"
echo "kubectl logs -n skala-practice -l app=trace-call-app | grep -i otel"
echo ""
echo "# 테스트 요청 보내기"
echo "kubectl run -n skala-practice curl-test --image=curlimages/curl:latest --rm -it --restart=Never -- \\"
echo "  curl -s http://trace-call-app:8080/api/test/all"
echo ""
echo "# Grafana에서 Traces 확인"
echo "https://observability.skala25a.project.skala-ai.com"
echo "  -> Explore -> Tempo -> service.name = trace-call-app"
echo ""

echo "=========================================="
echo "배포 완료!"
echo "=========================================="
