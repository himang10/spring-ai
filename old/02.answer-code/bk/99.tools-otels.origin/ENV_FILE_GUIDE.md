# .env 파일 사용 가이드

`.env.alloy` 파일을 사용하여 환경변수를 관리하는 방법을 설명합니다.

## 📋 파일 구조

```
.
├── .env.alloy.example    # 템플릿 (Git에 커밋됨)
├── .env.alloy            # 실제 설정 (Git에서 제외됨)
└── run-with-alloy.sh     # 자동으로 .env.alloy 로드
```

## 🚀 사용 방법 (3단계)

### 1단계: .env.alloy 파일 생성

```bash
# 템플릿 복사
cp .env.alloy.example .env.alloy
```

### 2단계: 실제 값 입력

```bash
# 편집기로 열기
vim .env.alloy

# 또는
nano .env.alloy
```

**입력 예시**:
```bash
# 필수
OPEN_AI_KEY=sk-proj-xxxxx...
WEATHER_API_KEY=your-weather-api-key

# Alloy OTLP
OTEL_EXPORTER_OTLP_ENDPOINT=https://alloy.skala25a.project.skala-ai.com
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
OTEL_SERVICE_NAME=spring-ai-tools
```

### 3단계: 실행

```bash
./run-with-alloy.sh
```

스크립트가 **자동으로 `.env.alloy` 파일을 읽어서** 환경변수로 설정합니다!

## 🔧 작동 원리

### run-with-alloy.sh의 자동 로딩 로직

```bash
# run-with-alloy.sh 일부

# .env.alloy 파일이 있으면 자동 로드
if [ -f .env.alloy ]; then
    echo "📁 .env.alloy 파일 발견 - 환경변수 로드 중..."

    # .env.alloy 파일에서 환경변수 로드
    # 주석(#)과 빈 줄 제외
    set -a
    source <(grep -v '^#' .env.alloy | grep -v '^$')
    set +a

    echo "✅ .env.alloy 파일에서 환경변수 로드 완료"
fi
```

**처리 과정**:
1. `.env.alloy` 파일 존재 확인
2. 주석(`#`으로 시작하는 줄) 제거
3. 빈 줄 제거
4. 나머지 줄을 환경변수로 설정 (`KEY=VALUE` → `export KEY=VALUE`)

## 📝 .env.alloy 파일 형식

### 기본 형식

```bash
# 주석은 #로 시작
KEY=VALUE

# 띄어쓰기 없이 작성
OPEN_AI_KEY=sk-xxxxx...

# 따옴표는 선택사항 (공백이 포함된 경우 필요)
OTEL_SERVICE_NAME=spring-ai-tools
OTEL_SERVICE_NAME="spring-ai-tools"  # 둘 다 가능
```

### 전체 예시

```bash
# ============================================
# 필수 환경변수
# ============================================

# OpenAI API Key
OPEN_AI_KEY=sk-proj-xxxxx...

# Weather API Key (선택사항)
WEATHER_API_KEY=your-weather-key

# ============================================
# Grafana Alloy OTLP 설정
# ============================================

# Alloy OTLP Endpoint
OTEL_EXPORTER_OTLP_ENDPOINT=https://alloy.skala25a.project.skala-ai.com

# OTLP 프로토콜
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf

# Traces 엔드포인트
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://alloy.skala25a.project.skala-ai.com/v1/traces

# Logs 엔드포인트
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://alloy.skala25a.project.skala-ai.com/v1/logs

# 서비스 이름
OTEL_SERVICE_NAME=spring-ai-tools

# Resource Attributes
OTEL_RESOURCE_ATTRIBUTES=service.name=spring-ai-tools,service.version=1.0.0,deployment.environment=local
```

## 🔄 로딩 우선순위

환경변수 값은 다음 우선순위로 결정됩니다:

1. **쉘에서 이미 설정된 환경변수** (최우선)
   ```bash
   export OPEN_AI_KEY=sk-manual...
   ./run-with-alloy.sh  # 이미 설정된 값 사용
   ```

2. **.env.alloy 파일** (쉘 환경변수가 없을 때)
   ```bash
   # .env.alloy에서 로드
   ./run-with-alloy.sh
   ```

3. **스크립트 기본값** (둘 다 없을 때)
   ```bash
   # 스크립트의 하드코딩된 기본값 사용
   ```

## 🔐 보안 주의사항

### ✅ 해야 할 것

```bash
# .env.alloy 파일을 .gitignore에 추가 (이미 추가됨)
echo ".env.alloy" >> .gitignore

# 파일 권한 제한
chmod 600 .env.alloy

# 템플릿만 Git에 커밋
git add .env.alloy.example
```

### ❌ 하지 말아야 할 것

```bash
# .env.alloy를 Git에 커밋하지 마세요!
git add .env.alloy  # ❌ 위험!

# API 키를 코드에 하드코딩하지 마세요!
export OPEN_AI_KEY="sk-xxxxx..."  # 스크립트에 이렇게 쓰지 마세요
```

## 🧪 테스트

### 1. .env.alloy 파일 생성 및 확인

```bash
# 파일 생성
cp .env.alloy.example .env.alloy

# 내용 확인
cat .env.alloy

# API 키 입력
vim .env.alloy
```

### 2. 환경변수 로딩 테스트

```bash
# 실행 (자동 로딩)
./run-with-alloy.sh
```

**예상 출력**:
```
============================================
Spring AI + Grafana Alloy (OTEL Collector)
============================================

📁 .env.alloy 파일 발견 - 환경변수 로드 중...

✅ .env.alloy 파일에서 환경변수 로드 완료

✅ OpenAI API Key 확인 완료
...
```

### 3. 환경변수 확인

```bash
# 로딩된 환경변수 확인 (별도 터미널)
source .env.alloy
env | grep -E 'OPEN_AI|OTEL'
```

## 🆚 방법 비교

### 방법 1: .env.alloy 파일 사용 (추천)

**장점**:
- ✅ API 키를 파일로 관리
- ✅ 자동으로 로드됨
- ✅ Git에서 제외 (안전)
- ✅ 팀원과 템플릿 공유 가능

**사용법**:
```bash
cp .env.alloy.example .env.alloy
vim .env.alloy  # API 키 입력
./run-with-alloy.sh  # 자동 로드
```

### 방법 2: export 명령 사용

**장점**:
- ✅ 간단함
- ✅ 일시적 사용에 적합

**단점**:
- ❌ 터미널 세션 종료 시 사라짐
- ❌ 매번 입력 필요

**사용법**:
```bash
export OPEN_AI_KEY="sk-xxxxx..."
export WEATHER_API_KEY="your-key"
./run-with-alloy.sh
```

### 방법 3: ~/.zshrc 또는 ~/.bashrc에 추가

**장점**:
- ✅ 영구 설정
- ✅ 모든 프로젝트에서 사용 가능

**단점**:
- ❌ 다른 사용자/환경에서 설정 필요
- ❌ 여러 프로젝트에서 다른 키 사용 시 불편

**사용법**:
```bash
echo 'export OPEN_AI_KEY="sk-xxxxx..."' >> ~/.zshrc
source ~/.zshrc
```

## 🐛 트러블슈팅

### 1. ".env.alloy 파일이 없습니다" 메시지

**원인**: `.env.alloy` 파일이 생성되지 않음

**해결**:
```bash
cp .env.alloy.example .env.alloy
vim .env.alloy  # 실제 값 입력
```

### 2. "OPEN_AI_KEY 환경변수가 설정되지 않았습니다"

**원인**: `.env.alloy` 파일에 `OPEN_AI_KEY`가 없거나 잘못됨

**해결**:
```bash
# .env.alloy 파일 확인
cat .env.alloy | grep OPEN_AI_KEY

# 값이 없으면 추가
echo 'OPEN_AI_KEY=sk-xxxxx...' >> .env.alloy
```

### 3. 환경변수가 로드되지 않음

**원인**: 파일 형식이 잘못됨

**확인사항**:
- [ ] `KEY=VALUE` 형식인가? (띄어쓰기 없이)
- [ ] 주석은 `#`으로 시작하는가?
- [ ] 빈 줄이 있는가?

**올바른 형식**:
```bash
OPEN_AI_KEY=sk-xxxxx...  # ✅
OPEN_AI_KEY = sk-xxxxx... # ❌ 띄어쓰기 금지
```

### 4. .env.alloy 파일이 Git에 추가됨

**해결**:
```bash
# Git에서 제거 (파일은 유지)
git rm --cached .env.alloy

# .gitignore 확인
grep .env.alloy .gitignore

# 없으면 추가
echo ".env.alloy" >> .gitignore
```

## 📚 참고

### 관련 파일

- [.env.alloy.example](.env.alloy.example) - 템플릿
- [run-with-alloy.sh](run-with-alloy.sh) - 자동 로딩 스크립트
- [.gitignore](.gitignore) - `.env.alloy` 제외 설정

### 환경변수 목록

| 환경변수 | 필수 | 기본값 | 설명 |
|---------|------|--------|------|
| `OPEN_AI_KEY` | ✅ | - | OpenAI API 키 |
| `WEATHER_API_KEY` | ⚠️ | `not-configured` | Weather API 키 |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | ⚠️ | `https://alloy...` | Alloy 엔드포인트 |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | ❌ | `http/protobuf` | OTLP 프로토콜 |
| `OTEL_SERVICE_NAME` | ❌ | `spring-ai-tools` | 서비스 이름 |

## 💡 팁

### 프로젝트별 .env 파일

```bash
# 프로젝트마다 다른 설정 사용
project-a/.env.alloy  # 프로젝트 A 설정
project-b/.env.alloy  # 프로젝트 B 설정
```

### 환경별 .env 파일

```bash
# 환경별로 다른 파일 사용
.env.alloy.dev      # 개발 환경
.env.alloy.staging  # 스테이징 환경
.env.alloy.prod     # 프로덕션 환경

# 사용할 파일 복사
cp .env.alloy.dev .env.alloy
./run-with-alloy.sh
```

## 🎯 요약

```bash
# 1. 템플릿 복사
cp .env.alloy.example .env.alloy

# 2. API 키 입력
vim .env.alloy

# 3. 실행 (자동 로딩!)
./run-with-alloy.sh
```

**끝!** `.env.alloy` 파일이 자동으로 로드되어 환경변수로 설정됩니다.
