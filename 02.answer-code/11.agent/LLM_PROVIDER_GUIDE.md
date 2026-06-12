# LLM 프로바이더 선택 가이드

이 프로젝트는 **OpenAI**와 **Google Gemini** 두 가지 LLM을 선택하여 사용할 수 있습니다.

## 1. LLM 프로바이더 선택

`src/main/resources/application.properties` 파일에서 설정:

```properties
## LLM 선택 (openai 또는 gemini)
llm.provider=openai
```

### OpenAI 사용 (기본값)
```properties
llm.provider=openai
```

### Google Gemini 사용
```properties
llm.provider=gemini
```

## 2. OpenAI 설정

### API 키 발급
1. https://platform.openai.com/ 접속
2. 로그인 후 "API keys" 메뉴
3. "Create new secret key" 클릭

### 환경 변수 설정

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-..."
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY="sk-..."
```

### 모델 선택
```properties
spring.ai.openai.chat.options.model=gpt-4o-mini
```

사용 가능한 모델:
- `gpt-4o` - 최신 고성능 모델
- `gpt-4o-mini` - 경제적인 모델 (기본값)
- `gpt-4-turbo` - GPT-4 Turbo
- `gpt-3.5-turbo` - 가장 저렴한 모델

## 3. Google Gemini 설정

### Google Cloud 프로젝트 설정

1. **Google Cloud Console 접속**
   - https://console.cloud.google.com/

2. **프로젝트 생성**
   - 새 프로젝트 생성 또는 기존 프로젝트 선택

3. **Vertex AI API 활성화**
   - "APIs & Services" → "Enable APIs and Services"
   - "Vertex AI API" 검색 후 활성화

4. **서비스 계정 생성**
   - "IAM & Admin" → "Service Accounts"
   - "Create Service Account"
   - 역할: "Vertex AI User" 추가

5. **인증 키 다운로드**
   - 생성된 서비스 계정 클릭
   - "Keys" 탭 → "Add Key" → "Create new key"
   - JSON 형식 선택하여 다운로드

### 환경 변수 설정

**Windows (PowerShell):**
```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\service-account-key.json"
$env:GEMINI_PROJECT_ID="your-project-id"
$env:GEMINI_LOCATION="us-central1"
```

**Linux/Mac:**
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account-key.json"
export GEMINI_PROJECT_ID="your-project-id"
export GEMINI_LOCATION="us-central1"
```

### application.properties 설정

```properties
spring.ai.vertex.ai.gemini.project-id=${GEMINI_PROJECT_ID}
spring.ai.vertex.ai.gemini.location=${GEMINI_LOCATION:us-central1}
spring.ai.vertex.ai.gemini.chat.options.model=gemini-1.5-flash
```

사용 가능한 모델:
- `gemini-1.5-pro` - 고성능 모델
- `gemini-1.5-flash` - 빠르고 경제적 (기본값)
- `gemini-1.0-pro` - 이전 버전

사용 가능한 리전:
- `us-central1` (기본값)
- `europe-west1`
- `asia-northeast1`

## 4. 비용 비교

### OpenAI GPT-4o-mini
- **입력:** $0.15 / 1M tokens
- **출력:** $0.60 / 1M tokens
- 무료 크레딧: 신규 가입 시 제공

### Google Gemini 1.5 Flash
- **입력:** $0.075 / 1M tokens (128k 이하)
- **출력:** $0.30 / 1M tokens (128k 이하)
- 무료 할당량: 월 일정량 제공

## 5. 프로바이더별 장단점

### OpenAI
**장점:**
- ✅ 간단한 API 키 인증
- ✅ 뛰어난 한국어 성능
- ✅ 빠른 응답 속도
- ✅ 풍부한 문서와 커뮤니티

**단점:**
- ❌ Gemini보다 비용이 높음
- ❌ 토큰 제한이 있음

### Google Gemini
**장점:**
- ✅ 더 저렴한 비용
- ✅ 긴 컨텍스트 윈도우 (최대 2M tokens)
- ✅ Google Cloud 통합
- ✅ 무료 할당량 제공

**단점:**
- ❌ 복잡한 인증 설정 (서비스 계정)
- ❌ 한국 리전 없음 (지연 시간 증가 가능)
- ❌ OpenAI보다 문서가 적음

## 6. 실행 방법

### Gradle로 실행
```bash
./gradlew bootRun
```

### VSCode에서 실행
1. `DemoApplication.java` 파일 열기
2. Run 버튼 클릭 또는 F5

### 브라우저에서 테스트
http://localhost:8080

## 7. 프로바이더 전환 테스트

1. **OpenAI로 테스트**
   ```properties
   llm.provider=openai
   ```
   애플리케이션 재시작 → 테스트

2. **Gemini로 전환**
   ```properties
   llm.provider=gemini
   ```
   애플리케이션 재시작 → 테스트

3. **응답 비교**
   - 같은 질문으로 두 모델의 답변 품질 비교
   - 응답 속도, 정확도, 자연스러움 평가

## 8. 문제 해결

### OpenAI 인증 오류
```
401 Unauthorized
```
→ API 키 확인: https://platform.openai.com/api-keys

### Gemini 인증 오류
```
403 Permission denied
```
→ 서비스 계정 권한 확인 ("Vertex AI User" 역할 필요)

### Gemini 프로젝트 ID 오류
```
Project not found
```
→ `GEMINI_PROJECT_ID` 환경 변수 확인

## 9. 추천 사용 시나리오

### OpenAI 추천
- 🎯 빠른 프로토타이핑
- 🎯 한국어 품질이 중요한 경우
- 🎯 간단한 인증 설정 선호

### Gemini 추천
- 🎯 비용 최적화가 중요한 경우
- 🎯 긴 문서 처리 필요
- 🎯 Google Cloud 인프라 사용 중
- 🎯 프로덕션 환경 (무료 할당량 활용)

## 10. Agent별 다른 LLM 사용

현재는 전체 애플리케이션이 하나의 LLM을 사용하지만, 각 Agent마다 다른 LLM을 사용하려면:

```java
@Configuration
public class LlmConfig {
    
    @Bean
    @Qualifier("openaiBuilder")
    public ChatClient.Builder openaiChatClientBuilder(OpenAiChatModel model) {
        return ChatClient.builder(model);
    }
    
    @Bean
    @Qualifier("geminiBuilder")
    public ChatClient.Builder geminiChatClientBuilder(VertexAiGeminiChatModel model) {
        return ChatClient.builder(model);
    }
}

// Agent에서 사용
public WeatherAgent(@Qualifier("openaiBuilder") ChatClient.Builder builder) {
    // OpenAI 사용
}

public RestaurantAgent(@Qualifier("geminiBuilder") ChatClient.Builder builder) {
    // Gemini 사용
}
```
