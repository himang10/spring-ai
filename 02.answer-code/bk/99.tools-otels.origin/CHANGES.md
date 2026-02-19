# 변경 사항 (환경변수 방식으로 전환)

## ✅ 완료된 변경

### 1. Config 파일 제거

다음 파일들이 **삭제**되었습니다:
- ❌ `src/main/java/com/example/springai/config/LangfuseProperties.java`
- ❌ `src/main/java/com/example/springai/config/LangfuseAutoConfiguration.java`
- ❌ `LANGFUSE_CONFIG_GUIDE.md`

**이유**: 환경변수만으로 충분히 동작하므로 불필요한 코드 제거

### 2. application.yml 수정

```yaml
# 이전 (Config 파일 사용)
langfuse:
  enabled: ${LANGFUSE_ENABLED:false}
  public-key: ${LANGFUSE_PUBLIC_KEY:}
  secret-key: ${LANGFUSE_SECRET_KEY:}
  host: ${LANGFUSE_HOST:...}

# 현재 (환경변수만 사용)
# Langfuse 연동 (OpenTelemetry via 환경변수)
# OTEL_EXPORTER_OTLP_ENDPOINT=...
# OTEL_EXPORTER_OTLP_HEADERS=...
```

### 3. 새로운 파일 추가

- ✅ `setup-langfuse.sh` - 대화형 환경변수 설정 스크립트
- ✅ `LANGFUSE_ENV.md` - 환경변수 설정 가이드
- ✅ `README_LANGFUSE.md` - 빠른 시작 가이드
- ✅ `NO_CONFIG_GUIDE.md` - Config 없이 사용하는 방법

### 4. 실행 스크립트 업데이트

[run.sh](run.sh):
- OpenTelemetry 환경변수 확인
- 설정 방법 안내 개선

## 🚀 새로운 사용 방법

### 이전 (Config 파일 방식)

```yaml
# application.yml 수정
langfuse:
  enabled: true
  public-key: pk-lf-xxx
  secret-key: sk-lf-xxx
```

```bash
./run.sh
```

### 현재 (환경변수 방식)

```bash
# 1. 환경변수 설정 도우미 실행
./setup-langfuse.sh

# 2. 출력된 export 명령 실행
export OTEL_EXPORTER_OTLP_ENDPOINT=...
export OTEL_EXPORTER_OTLP_HEADERS=...

# 3. 애플리케이션 실행
./run.sh
```

또는 한 번에:

```bash
# Base64 인코딩
AUTH=$(echo -n "pk-lf-xxx:sk-lf-xxx" | base64)

# 환경변수 설정
export OTEL_EXPORTER_OTLP_ENDPOINT=https://langfuse.skala25a.project.skala-ai.com/api/public/otel
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic $AUTH"

# 실행
./run.sh
```

## 📊 비교

| 항목 | 이전 (Config 사용) | 현재 (환경변수만) |
|------|-------------------|------------------|
| Java Config 파일 | 2개 필요 | 0개 (불필요) |
| application.yml | langfuse 섹션 필요 | 주석만 (선택사항) |
| 환경변수 | 4개 (LANGFUSE_*) | 2개 (OTEL_*) |
| Base64 인코딩 | 자동 처리 | 수동 처리 필요 |
| 코드 복잡도 | 중간 | 매우 낮음 |
| 프로덕션 배포 | Kubernetes Secret | Kubernetes Secret |

## 💡 장점

### 환경변수 방식의 장점

1. **코드 간소화**
   - Config 클래스 불필요
   - application.yml 설정 불필요

2. **표준 준수**
   - OpenTelemetry 표준 환경변수 사용
   - 다른 OpenTelemetry 도구와 호환

3. **배포 유연성**
   - 환경별 설정 변경 용이
   - Kubernetes Secret 직접 매핑

4. **보안**
   - API 키가 코드에 없음
   - Git에 커밋될 위험 없음

## 📚 문서

| 문서 | 설명 |
|------|------|
| [README_LANGFUSE.md](README_LANGFUSE.md) | 빠른 시작 가이드 |
| [LANGFUSE_ENV.md](LANGFUSE_ENV.md) | 환경변수 설정 상세 가이드 |
| [LANGFUSE_SETUP.md](LANGFUSE_SETUP.md) | 전체 배포 가이드 |
| [NO_CONFIG_GUIDE.md](NO_CONFIG_GUIDE.md) | Config 없이 사용하는 이유 |

## 🔄 마이그레이션 가이드

기존 Config 방식을 사용하던 경우:

### 1단계: Config 파일 삭제 (이미 완료)

```bash
rm src/main/java/com/example/springai/config/LangfuseProperties.java
rm src/main/java/com/example/springai/config/LangfuseAutoConfiguration.java
```

### 2단계: application.yml 정리

```yaml
# langfuse 섹션 제거 또는 주석 처리
# langfuse:
#   enabled: ...
```

### 3단계: 환경변수 설정

```bash
./setup-langfuse.sh
```

### 4단계: 테스트

```bash
./run.sh
```

## ✅ 체크리스트

- [x] Config 파일 제거
- [x] application.yml 수정
- [x] setup-langfuse.sh 스크립트 추가
- [x] 문서 업데이트
- [x] run.sh 수정
- [x] 환경변수 가이드 작성

## 🎯 다음 단계

1. **환경변수 설정**:
   ```bash
   ./setup-langfuse.sh
   ```

2. **애플리케이션 실행**:
   ```bash
   ./run.sh
   ```

3. **Langfuse 확인**:
   https://langfuse.skala25a.project.skala-ai.com

---

**요약**: 이제 **Config 파일 없이** 환경변수만으로 Langfuse를 사용할 수 있습니다!
