# Spring AI + Langfuse 연동

환경변수만으로 Langfuse와 연동되는 Spring AI 애플리케이션입니다.

## 🚀 빠른 시작

### 1. Langfuse API 키 발급

1. https://langfuse.skala25a.project.skala-ai.com 접속
2. Sign Up → New Project → Settings → API Keys
3. Public Key와 Secret Key 복사

### 2. 환경변수 설정

#### 대화형 스크립트 사용 (추천)

```bash
./setup-langfuse.sh
```

출력되는 `export` 명령을 복사해서 실행합니다.

#### 수동 설정

```bash
# Base64 인코딩
AUTH=$(echo -n "pk-lf-YOUR_PUBLIC:sk-lf-YOUR_SECRET" | base64)

# 환경변수 설정
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"
```

### 3. 실행

```bash
./run.sh
```

## ✅ 확인

### 로그 확인

```
✅ OpenAI API Key 확인 완료
✅ Langfuse (OpenTelemetry) 환경변수 설정 완료

Spring AI Tools 데모를 시작합니다.
```

### Langfuse 웹 UI

https://langfuse.skala25a.project.skala-ai.com → Traces 메뉴

## 📁 프로젝트 구조

```
.
├── src/
│   └── main/
│       ├── java/com/example/springai/
│       │   ├── controller/       # REST Controllers
│       │   ├── service/          # Business Logic
│       │   ├── weather/          # Weather Tool
│       │   ├── music/            # Music Tool
│       │   ├── datetime/         # DateTime Tool
│       │   └── filesystem/       # FileSystem Tool
│       └── resources/
│           └── application.yml   # 설정 파일 (Langfuse 주석 포함)
├── langfuse/
│   └── helm/                     # Langfuse Kubernetes 배포
│       ├── values.yaml
│       └── install.sh
├── run.sh                        # 실행 스크립트
├── setup-langfuse.sh             # Langfuse 환경변수 설정 도우미
├── LANGFUSE_ENV.md               # 환경변수 설정 가이드
└── LANGFUSE_SETUP.md             # 전체 설정 가이드

**Config 파일 없음!** - 환경변수만으로 동작
```

## 📚 문서

- **[LANGFUSE_ENV.md](LANGFUSE_ENV.md)** - 환경변수 설정 방법 (추천)
- **[LANGFUSE_SETUP.md](LANGFUSE_SETUP.md)** - 전체 배포 및 설정 가이드
- **[NO_CONFIG_GUIDE.md](NO_CONFIG_GUIDE.md)** - Config 없이 사용하는 방법

## 🔧 고급 설정

### 영구 환경변수 설정

```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
AUTH=$(echo -n "pk-lf-xxx:sk-lf-xxx" | base64)
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

source ~/.zshrc
```

### Kubernetes 배포

```yaml
env:
  - name: OTEL_EXPORTER_OTLP_ENDPOINT
    value: https://langfuse.skala25a.project.skala-ai.com/api/public/otel
  - name: OTEL_EXPORTER_OTLP_HEADERS
    valueFrom:
      secretKeyRef:
        name: langfuse-secret
        key: auth-header
```

## 🐛 트러블슈팅

### Langfuse에 데이터가 안 보임

```bash
# 환경변수 확인
echo $OTEL_EXPORTER_OTLP_ENDPOINT
echo $OTEL_EXPORTER_OTLP_HEADERS

# 다시 설정
./setup-langfuse.sh
```

### Base64 인코딩 오류

```bash
# -n 옵션 필수!
AUTH=$(echo -n "pk-lf-xxx:sk-lf-xxx" | base64)
```

## 📖 참고

- [Langfuse 공식 문서](https://langfuse.com/docs)
- [Spring AI 문서](https://docs.spring.io/spring-ai/reference/)
- [OpenTelemetry 환경변수](https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/)

---

**간단 요약**:
```bash
./setup-langfuse.sh  # API 키 입력
./run.sh             # 실행
```
