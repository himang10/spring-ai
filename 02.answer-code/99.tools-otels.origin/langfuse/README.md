# Langfuse Kubernetes 설치 가이드

Langfuse는 LLM Observability를 위한 오픈소스 플랫폼입니다.

## 설치 방법

두 가지 설치 방법을 제공합니다:

1. **Helm 방식** (권장): `helm/` 디렉토리 참조
   - 간편한 설치 및 업그레이드
   - 자동 의존성 관리 (PostgreSQL, Redis, ClickHouse)

2. **Kubernetes Manifest 방식**: `k8s/` 디렉토리 참조
   - 세밀한 제어 가능
   - Helm 없이 kubectl만으로 설치

## 요구사항

- Kubernetes 클러스터 (1.19+)
- kubectl CLI 도구
- Helm 3.x (Helm 방식 사용 시)
- Ingress Controller (외부 접속 시)

## 구성요소

- **Langfuse Web**: 메인 웹 애플리케이션
- **PostgreSQL**: 메타데이터 저장
- **ClickHouse**: 분석 데이터 저장
- **Redis**: 캐시 및 세션 관리
- **S3/MinIO**: 객체 스토리지

## 참고 자료

- [공식 문서](https://langfuse.com/self-hosting/deployment/kubernetes-helm)
- [GitHub 저장소](https://github.com/langfuse/langfuse-k8s)
