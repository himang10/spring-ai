# Langfuse 환경변수 설정 가이드

Spring AI + Langfuse 연동을 **환경변수만으로** 설정하는 간단한 방법입니다.

## 🚀 빠른 시작 (3단계)

### 1단계: Langfuse에서 API 키 발급

1. https://langfuse.skala25a.project.skala-ai.com 접속
2. **Sign Up** (계정 생성)
3. **New Project** 생성
4. **Settings** → **API Keys** → **Create new API Key**
5. Public Key와 Secret Key 복사

```
Public Key:  pk-lf-1234567890abcdef...
Secret Key:  sk-lf-abcdef1234567890...
```

### 2단계: 환경변수 설정

#### 방법 A) 대화형 스크립트 사용 (추천)

```bash
chmod +x setup-langfuse.sh
./setup-langfuse.sh
```

스크립트가 출력하는 `export` 명령을 복사해서 실행합니다.

#### 방법 B) 수동 설정

```bash
# 1. Base64 인코딩
AUTH=$(echo -n "pk-lf-YOUR_PUBLIC_KEY:sk-lf-YOUR_SECRET_KEY" | base64)

# 2. 환경변수 설정
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 3. 확인
echo $OTEL_EXPORTER_OTLP_ENDPOINT
echo $OTEL_EXPORTER_OTLP_HEADERS
```

### 3단계: 애플리케이션 실행

```bash
./run.sh
```

## ✅ 정상 작동 확인

### 실행 시 로그

```
✅ OpenAI API Key 확인 완료
✅ Langfuse (OpenTelemetry) 환경변수 설정 완료

============================================
Spring AI Tools 데모를 시작합니다.
============================================
```

### Langfuse에서 확인

1. https://langfuse.skala25a.project.skala-ai.com 접속
2. 프로젝트 선택
3. **Traces** 메뉴에서 데이터 확인

## 📝 전체 설정 예시

### 터미널 명령

```bash
# OpenAI API 키
export OPEN_AI_KEY="sk-xxxxx..."

# Weather API 키
export WEATHER_API_KEY="your-weather-key"

# Langfuse (Base64 인코딩 먼저)
export AUTH=$(echo -n "pk-lf-1234567890abcdef:sk-lf-abcdef1234567890" | base64)
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 실행
./run.sh
```

## 🔧 환경별 설정

### 로컬 개발 환경

매번 설정하기 번거로우므로 **~/.zshrc** (또는 **~/.bashrc**)에 추가:

```bash
# ~/.zshrc 파일에 추가
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-weather-key"

# Langfuse 설정
export AUTH=$(echo -n "pk-lf-dev-key:sk-lf-dev-secret" | base64)
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"
```

저장 후 적용:
```bash
source ~/.zshrc
```

### Docker 환경

```dockerfile
# Dockerfile
FROM openjdk:21-jdk-slim

# 환경변수 설정
ENV OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
ENV OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic <base64_encoded>"

COPY target/spring-ai-tools.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

또는 docker-compose.yml:
```yaml
version: '3.8'
services:
  app:
    image: spring-ai-tools
    environment:
      - OPEN_AI_KEY=${OPEN_AI_KEY}
      - WEATHER_API_KEY=${WEATHER_API_KEY}
      - OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
      - OTEL_EXPORTER_OTLP_HEADERS=Authorization=Basic ${LANGFUSE_AUTH_BASE64}
```

### Kubernetes 환경

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: langfuse-secret
type: Opaque
stringData:
  otel-endpoint: https://langfuse.skala25a.project.skala-ai.com/api/public/otel
  otel-headers: Authorization=Basic <base64_encoded>
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-ai-app
spec:
  template:
    spec:
      containers:
      - name: app
        image: spring-ai-tools:latest
        env:
        - name: OPEN_AI_KEY
          valueFrom:
            secretKeyRef:
              name: api-keys
              key: openai-key
        - name: OTEL_EXPORTER_OTLP_ENDPOINT
          valueFrom:
            secretKeyRef:
              name: langfuse-secret
              key: otel-endpoint
        - name: OTEL_EXPORTER_OTLP_HEADERS
          valueFrom:
            secretKeyRef:
              name: langfuse-secret
              key: otel-headers
```

## 🔒 보안 권장사항

### ❌ 하지 말아야 할 것

```bash
# Git에 커밋하지 마세요!
# .env 파일을 Git에 추가하지 마세요!
```

### ✅ 권장하는 방법

#### .env 파일 사용 (Git에서 제외)

```bash
# .env 파일 생성
cat > .env << EOF
OPEN_AI_KEY=sk-xxxxx...
WEATHER_API_KEY=your-key
OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
OTEL_EXPORTER_OTLP_HEADERS=Authorization=Basic <base64>
EOF

# .gitignore에 추가
echo ".env" >> .gitignore

# 환경변수 로드
export $(cat .env | xargs)

# 실행
./run.sh
```

#### .env.example 템플릿 제공

```bash
# .env.example (Git에 커밋 가능)
OPEN_AI_KEY=your-openai-key-here
WEATHER_API_KEY=your-weather-key-here
OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
OTEL_EXPORTER_OTLP_HEADERS=Authorization=Basic <base64_of_pk:sk>

# 사용법:
# 1. cp .env.example .env
# 2. .env 파일을 실제 값으로 수정
# 3. export $(cat .env | xargs)
```

## 🐛 트러블슈팅

### 1. "환경변수가 설정되지 않았습니다" 경고

**원인**: 환경변수가 설정되지 않음

**해결**:
```bash
# 현재 세션 확인
env | grep OTEL

# 환경변수 설정
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic <base64>"
```

### 2. Base64 인코딩 오류

**증상**: 401 Unauthorized 에러

**해결**:
```bash
# 올바른 Base64 인코딩 확인
PUBLIC_KEY="pk-lf-xxxxx..."
SECRET_KEY="sk-lf-xxxxx..."
AUTH=$(echo -n "$PUBLIC_KEY:$SECRET_KEY" | base64)
echo $AUTH

# -n 옵션 필수 (개행 문자 제거)
```

### 3. 환경변수가 사라짐

**원인**: 터미널 세션이 종료되면 환경변수도 사라짐

**해결**: ~/.zshrc 또는 ~/.bashrc에 영구 설정
```bash
echo 'export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel' >> ~/.zshrc
echo 'export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic <base64>"' >> ~/.zshrc
source ~/.zshrc
```

### 4. Langfuse에 데이터가 보이지 않음

**체크리스트**:

1. **환경변수 확인**:
   ```bash
   echo $OTEL_EXPORTER_OTLP_ENDPOINT
   echo $OTEL_EXPORTER_OTLP_HEADERS
   ```

2. **Base64 인코딩 확인**:
   ```bash
   # 다시 생성
   AUTH=$(echo -n "pk-lf-xxx:sk-lf-xxx" | base64)
   echo $AUTH
   ```

3. **네트워크 확인**:
   ```bash
   curl -I https://langfuse.skala25a.project.skala-ai.com
   ```

4. **Langfuse 서버 상태**:
   ```bash
   kubectl get pods -n langfuse
   ```

## 📚 참고 자료

- [OpenTelemetry 환경변수](https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/)
- [Langfuse 공식 문서](https://langfuse.com/docs)
- [Spring AI Observability](https://docs.spring.io/spring-ai/reference/observability/index.html)

## 💡 요약

### 필수 환경변수

```bash
# Langfuse 연동에 필요한 2개의 환경변수
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic <base64_of_pk:sk>"
```

### Base64 인코딩 방법

```bash
# Public Key: pk-lf-xxxxx...
# Secret Key: sk-lf-xxxxx...
AUTH=$(echo -n "pk-lf-xxxxx:sk-lf-xxxxx" | base64)
echo $AUTH
```

### 빠른 설정

```bash
./setup-langfuse.sh  # 대화형 스크립트
```

**끝!** 이제 환경변수만으로 Langfuse를 사용할 수 있습니다.
