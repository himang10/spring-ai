# 인터넷 서비스 API 응답 구조 가이드

이 문서는 이 프로젝트에서 사용하는 외부 API(SerpAPI Google 검색, SerpAPI YouTube 검색, OpenWeatherMap)의 응답 JSON 구조를 설명합니다.

---

## 1. SerpAPI - Google 검색 응답

**사용 위치:** `SerpApiSearchService.search()`, `Exam03AttractionAgent`, `Exam04RestaurantAgent`, `Exam05AccommodationAgent`

**API 호출 예시:**
```
GET https://serpapi.com/search
  ?engine=google
  &q=제주도 관광지
  &api_key={API_KEY}
```

**응답 JSON 구조:**
```json
{
  "search_metadata": {
    "id": "abc123",
    "status": "Success",
    "created_at": "2024-01-01 00:00:00 UTC",
    "total_time_taken": 1.23,
    "google_url": "https://www.google.com/search?q=제주도+관광지"
  },

  "search_parameters": {
    "engine": "google",
    "q": "제주도 관광지",
    "api_key": "..."
  },

  "search_information": {
    "total_results": 1230000000,
    "query_displayed": "제주도 관광지"
  },

  "ads": [
    {
      "position": 1,
      "title": "광고 제목",
      "link": "https://ad-link.com"
    }
  ],

  "organic_results": [
    {
      "position": 1,
      "title": "제주도 여행 필수 관광지 TOP 10",
      "link": "https://example.com/jeju-tour",
      "displayed_link": "example.com › jeju",
      "snippet": "제주도 대표 관광지로는 한라산, 성산일출봉...",
      "date": "2024년 1월",
      "thumbnail": "https://encrypted-tbn0.gstatic.com/...",
      "sitelinks": {
        "inline": [
          { "title": "한라산", "link": "https://example.com/hallasan" },
          { "title": "성산일출봉", "link": "https://example.com/seongsan" }
        ]
      }
    },
    {
      "position": 2,
      "title": "제주 관광 명소 추천 - 여행 블로그",
      "link": "https://blog.example.com/jeju",
      "displayed_link": "blog.example.com",
      "snippet": "제주도에는 자연, 문화, 체험 등 다양한 관광지가 있습니다..."
    },
    {
      "position": 3,
      "title": "제주특별자치도 공식 관광 안내",
      "link": "https://www.visitjeju.net",
      "snippet": "성산일출봉 입장료 2,000원, 한라산 무료입장..."
    }
  ],

  "related_questions": [
    {
      "question": "제주도 가볼만한 곳은?",
      "snippet": "성산일출봉, 한라산, 협재해수욕장...",
      "link": "https://example.com"
    }
  ],

  "related_searches": [
    { "query": "제주도 숨은 명소" },
    { "query": "제주도 겨울 여행" }
  ],

  "pagination": {
    "current": 1,
    "next": "https://serpapi.com/search?q=...&start=10"
  }
}
```

**코드에서 사용하는 필드 (`organic_results` 상위 3개):**

| 필드 | 경로 | 설명 |
|------|------|------|
| 제목 | `organic_results[n].title` | 검색 결과 페이지 제목 |
| URL | `organic_results[n].link` | 실제 웹 페이지 URL (fetch 도구에 전달) |
| 요약 | `organic_results[n].snippet` | 검색 결과 미리보기 텍스트 |

**LLM에 전달되는 최종 텍스트 형식:**
```
1. 제주도 여행 필수 관광지 TOP 10
https://example.com/jeju-tour
제주도 대표 관광지로는 한라산, 성산일출봉...

2. 제주 관광 명소 추천 - 여행 블로그
https://blog.example.com/jeju
제주도에는 자연, 문화, 체험 등 다양한 관광지가 있습니다...

3. 제주특별자치도 공식 관광 안내
https://www.visitjeju.net
성산일출봉 입장료 2,000원, 한라산 무료입장...
```

> **참고:** LLM은 이 결과에 포함된 `link` URL을 `fetchAttractionInfo` 도구에 전달하여 페이지 상세 본문을 추가로 조회합니다.

---

## 2. SerpAPI - YouTube 검색 응답

**사용 위치:** `Exam06YoutubeSearchAgent.searchYoutubeVideos()`

**API 호출 예시:**
```
GET https://serpapi.com/search
  ?engine=youtube
  &search_query=서울 여행
  &api_key={API_KEY}
```

**응답 JSON 구조:**
```json
{
  "search_metadata": {
    "status": "Success",
    "youtube_url": "https://www.youtube.com/results?search_query=서울+여행"
  },

  "search_parameters": {
    "engine": "youtube",
    "search_query": "서울 여행"
  },

  "video_results": [
    {
      "position": 1,
      "title": "서울 여행 브이로그 - 경복궁부터 홍대까지",
      "link": "https://www.youtube.com/watch?v=abc123",
      "published_date": "1 year ago",
      "views": 125000,
      "length": "12:34",
      "description": "서울의 숨겨진 명소를 소개합니다...",
      "channel": {
        "name": "여행유튜버",
        "link": "https://www.youtube.com/channel/UCabc123",
        "verified": true,
        "subscribers": "50K",
        "thumbnail": "https://yt3.ggpht.com/..."
      },
      "thumbnail": {
        "static": "https://i.ytimg.com/vi/abc123/hqdefault.jpg",
        "rich": "https://i.ytimg.com/vi/abc123/maxresdefault.jpg"
      }
    },
    {
      "position": 2,
      "title": "2024 서울 여행 완벽 가이드",
      "link": "https://www.youtube.com/watch?v=def456",
      "published_date": "6 months ago",
      "views": 89000,
      "length": "18:22",
      "description": "서울 여행 준비부터 맛집까지 모든 것을 알려드립니다...",
      "channel": {
        "name": "Korea Travel",
        "verified": false,
        "subscribers": "12K"
      }
    }
  ],

  "shorts_results": [
    {
      "title": "서울 야경 SHORTS",
      "link": "https://www.youtube.com/shorts/xyz789",
      "views": 500000
    }
  ],

  "ads": []
}
```

**코드에서 사용하는 필드 (`video_results` 전체):**

| 필드 | 경로 | 설명 |
|------|------|------|
| 제목 | `video_results[n].title` | 비디오 제목 |
| URL | `video_results[n].link` | YouTube 비디오 링크 |
| 업로드일 | `video_results[n].published_date` | 업로드 상대 시간 (예: "1 year ago") |

**LLM에 전달되는 최종 JSON 형식 (`formatVideosAsJson()` 출력):**
```json
[
  {"title":"서울 여행 브이로그 - 경복궁부터 홍대까지","uploadDate":"1 year ago","link":"https://www.youtube.com/watch?v=abc123"},
  {"title":"2024 서울 여행 완벽 가이드","uploadDate":"6 months ago","link":"https://www.youtube.com/watch?v=def456"}
]
```

> **참고:** `shorts_results`와 `ads`는 코드에서 사용하지 않습니다. `published_date`가 없으면 "날짜 정보 없음"으로 대체됩니다.

---

## 3. OpenWeatherMap - 현재 날씨 응답

**사용 위치:** `Exam02WeatherAgent.getWeatherInfo()`

**API 호출 예시:**
```
GET https://api.openweathermap.org/data/2.5/weather
  ?q=Seoul
  &appid={API_KEY}
  &units=metric
  &lang=kr
```

**응답 JSON 구조:**
```json
{
  "coord": {
    "lon": 126.9778,
    "lat": 37.5683
  },

  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "맑음",
      "icon": "01d"
    }
  ],

  "base": "stations",

  "main": {
    "temp": 23.5,
    "feels_like": 22.8,
    "temp_min": 21.0,
    "temp_max": 25.2,
    "pressure": 1013,
    "humidity": 60
  },

  "visibility": 10000,

  "wind": {
    "speed": 3.5,
    "deg": 270
  },

  "clouds": {
    "all": 5
  },

  "dt": 1704067200,

  "sys": {
    "country": "KR",
    "sunrise": 1704040000,
    "sunset": 1704077000
  },

  "timezone": 32400,
  "id": 1835848,
  "name": "Seoul",
  "cod": 200
}
```

**코드에서 사용하는 필드:**

| 필드 | 경로 | 설명 |
|------|------|------|
| 현재 온도 | `main.temp` | 현재 기온 (°C, metric 단위) |
| 체감 온도 | `main.feels_like` | 체감 온도 (°C) |
| 최저 온도 | `main.temp_min` | 당일 최저 기온 |
| 최고 온도 | `main.temp_max` | 당일 최고 기온 |
| 습도 | `main.humidity` | 습도 (%) |
| 날씨 상태(영문) | `weather[0].main` | 영문 상태 (예: Clear, Rain) |
| 날씨 상태(한글) | `weather[0].description` | 한글 설명 (`lang=kr` 지정 시) |

**LLM에 전달되는 최종 텍스트 형식:**
```
[날씨 정보]
도시: Seoul
날짜: 2024-01-01
날씨: 맑음 (Clear)
온도: 23.5°C
습도: 60%
체감온도: 22.8°C
최저/최고: 21.0°C / 25.2°C
```

---

## 4. OpenWeatherMap - 5일 예보 응답

**사용 위치:** `Exam02WeatherAgent.getWeeklyForecast()`

**API 호출 예시:**
```
GET https://api.openweathermap.org/data/2.5/forecast
  ?q=Seoul
  &appid={API_KEY}
  &units=metric
  &lang=kr
```

**응답 JSON 구조:**
```json
{
  "cod": "200",
  "message": 0,
  "cnt": 40,
  "city": {
    "id": 1835848,
    "name": "Seoul",
    "country": "KR",
    "timezone": 32400,
    "sunrise": 1704040000,
    "sunset": 1704077000
  },

  "list": [
    {
      "dt": 1704067200,
      "dt_txt": "2024-01-01 12:00:00",
      "main": {
        "temp": 23.5,
        "feels_like": 22.8,
        "temp_min": 21.0,
        "temp_max": 25.2,
        "humidity": 60,
        "pressure": 1013
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "맑음",
          "icon": "01d"
        }
      ],
      "wind": {
        "speed": 3.5,
        "deg": 270
      },
      "pop": 0.1,
      "clouds": { "all": 5 }
    },
    {
      "dt": 1704078000,
      "dt_txt": "2024-01-01 15:00:00",
      "main": {
        "temp": 25.0,
        "temp_min": 23.5,
        "temp_max": 26.0,
        "humidity": 55
      },
      "weather": [
        { "main": "Clouds", "description": "구름 조금" }
      ],
      "pop": 0.2
    }
  ]
}
```

**코드에서 사용하는 필드 (`list` 배열, 3시간 간격 40개 항목):**

| 필드 | 경로 | 설명 |
|------|------|------|
| 날짜/시간 | `list[n].dt_txt` | "yyyy-MM-dd HH:mm:ss" 형식 |
| 날짜 | `dt_txt.split(" ")[0]` | 날짜 부분만 추출 |
| 시간 | `dt_txt.split(" ")[1]` | 시간 부분만 추출 |
| 현재 온도 | `list[n].main.temp` | 해당 시간대 기온 |
| 최저/최고 | `list[n].main.temp_min/max` | 해당 시간대 최저/최고 |
| 날씨 상태 | `list[n].weather[0].description` | 한글 날씨 설명 |

**LLM에 전달되는 최종 텍스트 형식:**
```
[Seoul 5일 날씨 예보]

01/01(월): 최고 26.0°C / 최저 21.0°C
  12:00 - 맑음 23.5°C
  15:00 - 구름 조금 25.0°C

01/02(화): 최고 22.0°C / 최저 18.5°C
  ...
```

---

## API 비교 요약

| API | 제공사 | 엔드포인트 | 주요 반환 데이터 | 사용 에이전트 |
|-----|--------|-----------|-----------------|--------------|
| Google 검색 | SerpAPI | `serpapi.com/search?engine=google` | title, link, snippet | Exam03~05 |
| YouTube 검색 | SerpAPI | `serpapi.com/search?engine=youtube` | title, link, published_date | Exam06 |
| 현재 날씨 | OpenWeatherMap | `openweathermap.org/data/2.5/weather` | temp, humidity, description | Exam02 |
| 5일 예보 | OpenWeatherMap | `openweathermap.org/data/2.5/forecast` | dt_txt, temp, description (3시간 간격) | Exam02 |
