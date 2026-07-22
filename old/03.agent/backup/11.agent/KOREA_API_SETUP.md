# 한국관광공사 API 설정 가이드

## ⚠️ 현재 상태

**500 에러 원인**: application.properties의 API 키가 유효하지 않습니다.

## 📋 해결 단계

### 1단계: 공공데이터포털에서 API 키 발급/확인

1. **[공공데이터포털](https://www.data.go.kr/)** 접속 및 로그인

2. **API 신청** (아직 신청 안 했다면):
   - 상단 검색창에 "한국관광공사 국문 관광정보" 검색
   - "한국관광공사_국문 관광정보 서비스_GW" 선택
   - **활용신청** 버튼 클릭
   - 필수 정보 입력 후 신청
   - ⏳ **승인까지 1~2시간 대기**

3. **API 키 확인**:
   - **마이페이지** → **오픈API** → **개발계정**
   - "한국관광공사_국문 관광정보 서비스" 찾기
   - 상태가 **"승인"** 인지 확인
   - **⭐ "일반 인증키(Decoding)"** 값 복사 (Encoding 키 아님!)

### 2단계: 환경변수 설정

#### macOS/Linux:

```bash
# ~/.zshrc 또는 ~/.bash_profile에 추가
export KOREA_TOUR_API_KEY='여기에_Decoding_키_붙여넣기'

# 설정 적용
source ~/.zshrc  # 또는 source ~/.bash_profile
```

#### 또는 터미널에서 직접 실행:

```bash
export KOREA_TOUR_API_KEY='여기에_Decoding_키_붙여넣기'
```

### 3단계: API 테스트

```bash
# 테스트 스크립트 실행
./test-korea-api.sh
```

**예상 결과** (성공 시):
```json
{
  "response": {
    "header": {
      "resultCode": "0000",
      "resultMsg": "OK"
    },
    "body": {
      "items": {
        "item": [...]
      },
      "totalCount": 123
    }
  }
}
```

**실패 시**:
- `HTTP 상태 코드: 500` + `Unexpected errors` → API 키 오류
- `resultCode: "12"` → 필수 파라미터 누락
- `resultCode: "30"` → API 한도 초과 (일 1,000건)

### 4단계: 애플리케이션 실행

```bash
# Maven으로 실행
mvn spring-boot:run

# 또는 JAR로 실행
java -jar target/agent-0.0.1-SNAPSHOT.jar
```

## 🔍 문제 해결

### API 키가 여전히 작동하지 않는 경우:

1. **승인 상태 재확인**
   - 공공데이터포털 → 마이페이지 → 오픈API
   - 상태: "승인" ✅ (대기중 ❌, 반려 ❌)

2. **올바른 키 사용 확인**
   - ✅ 일반 인증키(**Decoding**) 사용
   - ❌ 일반 인증키(Encoding) 사용하지 말 것

3. **API 할당량 확인**
   - 일일 1,000건 제한
   - 마이페이지에서 사용량 확인

4. **서비스 키 활성화 대기**
   - 승인 직후에는 1~2시간 대기 필요할 수 있음

## 📝 참고사항

- **API 문서**: https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101578
- **일일 호출 제한**: 1,000건 (무료)
- **응답 형식**: JSON, XML
- **인증 방식**: Service Key

## 🆘 추가 지원

문제가 계속되면:
1. 공공데이터포털 고객센터: 1577-0042
2. 한국관광공사 TourAPI 고객센터: tour_api@knto.or.kr
