# 한국관광공사 TourAPI 검색 서비스

## 📋 개요

`KoreaTourApiSearchService`는 한국관광공사에서 제공하는 공공 데이터 API를 활용하여 전국의 관광지, 문화시설, 숙박, 음식점 등을 검색할 수 있는 서비스입니다.

## 🌟 주요 기능

### 1. 키워드 검색 (`search`)
- 관광지명, 지역명, 명소 등으로 검색
- 최대 8개의 검색 결과 반환
- 주소, 연락처, 관광지 타입 정보 제공

### 2. 상세 정보 조회 (`fetch`)
- contentId로 상세 정보 조회
- 입장료, 주차 정보, 운영 시간, 휴무일 등 제공
- 유모차/반려동물 동반 가능 여부 안내

## 🔧 설정

### application.properties
```properties
## 한국관광공사 TourAPI
korea.tour.api.endpoint=http://apis.data.go.kr/B551011/KorService1
korea.tour.api.key=6a012aa16f4de7525628f7eb87abfb4b349861f1708bd088a1a0b33ec5827b7e
```

### API 키 발급 방법
1. [공공데이터포털](https://www.data.go.kr/) 회원가입
2. "한국관광공사_국문 관광정보 서비스" 검색
3. 활용신청 → 승인 후 API 키 발급
4. 일일 호출 제한: 1,000건 (무료)

## 💡 사용 예제

### Spring AI Agent와 함께 사용
```java
// Agent에 자동으로 도구로 등록됨
@Service
public class TravelAgent {
    private final InternetSearchService searchService;
    
    // KoreaTourApiSearchService가 자동 주입
    public TravelAgent(InternetSearchService searchService) {
        this.searchService = searchService;
    }
}
```

### 직접 호출
```java
@Autowired
private KoreaTourApiSearchService tourApiService;

// 검색
String results = tourApiService.search("제주도 관광지");
// 결과:
// 🔍 '제주도 관광지' 검색 결과 (총 150건)
//
// [1] 성산일출봉 (관광지)
//    📍 제주특별자치도 서귀포시 성산읍 일출로 284-12
//    ☎️  064-783-0959
//    🔗 상세정보 ID: 264328
// ...

// 상세 정보 조회
String detail = tourApiService.fetch("264328");
// 결과:
// 📋 관광지 상세 정보
//
// 🏛️  성산일출봉
// 📝 유네스코 세계자연유산으로 지정된...
// 📍 주소: 제주특별자치도 서귀포시 성산읍 일출로 284-12
// ☎️  연락처: 064-783-0959
// ⏰ 이용시간: 07:30~20:00
// 🅿️  주차: 가능
```

## 🗺️ 관광 타입 코드

| 코드 | 설명 |
|------|------|
| 12 | 관광지 |
| 14 | 문화시설 |
| 15 | 축제/공연/행사 |
| 25 | 여행코스 |
| 28 | 레포츠 |
| 32 | 숙박 |
| 38 | 쇼핑 |
| 39 | 음식점 |

## 📊 검색 예제

### 지역별 검색
```
"제주도 관광지"
"부산 해운대"
"경주 불국사"
"서울 명동"
"강원도 스키장"
```

### 관광지명 검색
```
"남산타워"
"에버랜드"
"롯데월드"
"63빌딩"
"한라산"
```

### 테마별 검색
```
"제주 카페"
"부산 맛집"
"경주 문화재"
"강릉 해변"
"전주 한옥마을"
```

## 🔄 DummySearchService와 비교

| 항목 | DummySearchService | KoreaTourApiSearchService |
|------|-------------------|---------------------------|
| 데이터 소스 | 하드코딩 | 한국관광공사 공식 API |
| 검색 범위 | 제주도 18개 | 전국 수만 개 |
| 업데이트 | 수동 | 실시간 |
| 상세 정보 | 제한적 | 입장료, 주차, 운영시간 등 풍부 |
| API 호출 | 없음 | 있음 (1,000건/일) |
| 네트워크 | 불필요 | 필요 |

## 🚀 실행 방법

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 또는
java -jar build/libs/agent-0.0.1-SNAPSHOT.jar
```

## 🧪 테스트

웹 브라우저에서 `http://localhost:8080` 접속 후:

1. **관광지 검색 테스트**
   - "제주도에서 가볼 만한 관광지 추천해줘"
   - "부산 해운대 근처 명소 알려줘"

2. **맛집 검색 테스트**
   - "제주도 맛집 추천"
   - "전주 한정식 맛집"

3. **숙박 검색 테스트**
   - "강릉 호텔 추천"
   - "제주도 리조트"

## ⚠️ 주의사항

1. **API 호출 제한**: 무료 플랜은 하루 1,000건 제한
2. **응답 시간**: 네트워크 상태에 따라 1-3초 소요
3. **데이터 정확성**: 일부 관광지는 정보가 부족할 수 있음
4. **HTML 태그**: 일부 응답에 HTML 태그 포함 (자동 제거 처리됨)

## 🔧 트러블슈팅

### API 호출 실패
- API 키 확인: application.properties의 korea.tour.api.key 값 확인
- 네트워크 연결 확인
- 호출 제한 초과 여부 확인

### 검색 결과 없음
- 검색 키워드 변경 (예: "제주" → "제주도")
- 다른 지역/관광지로 시도

### 상세 정보 조회 실패
- contentId 값이 정확한지 확인
- 해당 관광지의 상세 정보가 등록되어 있는지 확인

## 📚 참고 자료

- [한국관광공사 TourAPI 문서](http://api.visitkorea.or.kr/)
- [공공데이터포털](https://www.data.go.kr/)
- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)

## 🎯 향후 개선 사항

- [ ] 캐싱 기능 추가 (Redis)
- [ ] 페이징 지원
- [ ] 이미지 다운로드 및 표시
- [ ] 지도 연동 (위도/경도 활용)
- [ ] 주변 관광지 추천
- [ ] 다국어 지원 (영어, 중국어, 일본어)
