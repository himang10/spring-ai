# Spring AI Tools Playground

Spring AI의 Function Calling 기능을 활용하여 LLM이 외부 도구(Tools)를 호출하는 다양한 시나리오를 실험할 수 있는 Spring Boot 애플리케이션입니다.
`@Tool` 어노테이션을 사용하여 Java 메서드를 도구로 등록하고, `ChatClient`를 통해 LLM이 이를 자연스럽게 호출하도록 구성되어 있습니다.

## 특징 요약

- **Function Calling 실습**: 날짜/시간, 날씨, 파일 시스템, 음악 등 다양한 도메인의 도구 구현
- **패키지 구조화**: 각 도구별로 독립적인 패키지(`datetime`, `weather`, `filesystem`, `music`) 구성
- **프론트엔드 연동**: `/` 페이지에서 도구를 선택하고 대화할 수 있는 UI 제공

## 서비스/엔드포인트 일람

| 구분 | Service | 핵심 기능 | 엔드포인트 |
|------|---------|-----------|------------|
| 날짜/시간 도구 | `DateTimeService` | 현재 시간 조회, 알람 설정 | `POST /chat/date-time-tools` |
| 날씨 도구 | `WeatherService` | OpenWeatherMap API 연동, 날씨 조회 | `POST /chat/weather-tools` |
| 파일 시스템 도구 | `FileSystemService` | 파일 저장, 목록 조회, 삭제 | `POST /chat/file-system-tools` |
| 음악 도구 | `MusicService` | 노래 검색, 플레이리스트 추가 | `POST /chat/music-tools` |


## 실행 전 준비

1. **환경 변수**
   ```bash
   export OPEN_AI_KEY=sk-...
   ```

2. **애플리케이션 실행**

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

접속: `http://localhost:8080`

## API 사용 예시

```bash
# 1. 날짜/시간 도구
curl -X POST http://localhost:8080/chat/date-time-tools \
  -d "message=지금 몇 시야?"

# 2. 날씨 도구
curl -X POST http://localhost:8080/chat/weather-tools \
  -d "message=서울 날씨 어때?"

# 3. 파일 시스템 도구
curl -X POST http://localhost:8080/chat/file-system-tools \
  -d "message=현재 디렉토리에 있는 파일 목록 보여줘"

# 4. 음악 도구
curl -X POST http://localhost:8080/chat/music-tools \
  -d "message=신나는 노래 찾아줘"
```

## 웹 UI 활용

- `home.html` + `chat.js` 조합으로 간단한 챗 인터페이스 제공
- Select Box에서 도구(엔드포인트)를 고르면 JS가 해당 URL로 `fetch` 요청을 보냅니다.
- 응답이 JSON이면 pretty-print 하여 보여주며, 텍스트 응답은 그대로 노출됩니다.

## 참고 자료

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Tools](https://docs.spring.io/spring-ai/reference/api/tools.html)
