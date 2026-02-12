# Spring AI - Structured Output Examples

Spring AI의 OutputConverter 기능을 사용하여 AI 응답을 다양한 구조화된 형태로 변환하는 예제 프로젝트입니다.

## 📋 프로젝트 개요

이 프로젝트는 Spring AI의 세 가지 주요 OutputConverter를 활용한 샘플 코드를 제공합니다:

1. **BeanOutputConverter** - Java 객체(Bean)로 변환
2. **MapOutputConverter** - Map<String, Object>로 변환
3. **ListOutputConverter** - List<String>로 변환

## 🛠 기술 스택

- Java 21
- Spring Boot 3.4.6
- Spring AI 1.1.0
- Thymeleaf
- Maven

## 📁 프로젝트 구조

```
03.structured-output/
├── pom.xml
├── src/main/
│   ├── java/com/example/springai/
│   │   ├── SpringAiStructuredOutputApplication.java
│   │   ├── controller/
│   │   │   ├── HomeController.java
│   │   │   └── OutputConverterController.java
│   │   ├── service/
│   │   │   ├── BeanOutputService.java
│   │   │   ├── MapOutputService.java
│   │   │   └── ListOutputService.java
│   │   └── model/
│   │       └── ActorsFilms.java
│   └── resources/
│       ├── application.yml
│       ├── static/
│       │   ├── css/home.css
│       │   └── js/home.js
│       └── templates/
│           └── home.html
```

## 🚀 시작하기

### 사전 요구사항

- JDK 21 이상
- Maven 3.6 이상
- OpenAI API Key

### 환경 설정

1. OpenAI API Key 설정:
```bash
export OPEN_AI_KEY=your-api-key-here
```

또는 `application.yml`에서 직접 설정:
```yaml
spring:
  ai:
    openai:
      api-key: your-api-key-here
```

### 빌드 및 실행

```bash
# 프로젝트 빌드
mvn clean package

# 애플리케이션 실행
mvn spring-boot:run
```

애플리케이션이 시작되면 브라우저에서 `http://localhost:8080`으로 접속하세요.

## 🎯 API 엔드포인트

### 1. BeanOutputConverter - Single Actor
**GET** `/chat/bean?actor={actorName}`

특정 배우의 영화 목록을 `ActorsFilms` Bean 객체로 반환합니다.

**예제:**
```bash
curl "http://localhost:8080/chat/bean?actor=Tom%20Hanks"
```

**응답:**
```json
{
  "type": "BeanOutputConverter",
  "actor": "Tom Hanks",
  "movies": [
    "Forrest Gump",
    "Saving Private Ryan",
    "Cast Away",
    "The Green Mile",
    "Toy Story"
  ]
}
```

### 2. BeanOutputConverter - Multiple Actors
**GET** `/chat/list-bean?actors={actorNames}`

여러 배우의 영화 목록을 `List<ActorsFilms>` 형태로 반환합니다.

**예제:**
```bash
curl "http://localhost:8080/chat/list-bean?actors=Tom%20Hanks%20and%20Bill%20Murray"
```

**응답:**
```json
{
  "type": "Generic Bean Type (List<ActorsFilms>)",
  "count": 2,
  "data": [
    {
      "actor": "Tom Hanks",
      "movies": ["Forrest Gump", "Saving Private Ryan", ...]
    },
    {
      "actor": "Bill Murray",
      "movies": ["Groundhog Day", "Lost in Translation", ...]
    }
  ]
}
```

### 3. MapOutputConverter
**GET** `/chat/map?subject={subject}`

요청한 정보를 `Map<String, Object>` 형태로 반환합니다.

**예제:**
```bash
curl "http://localhost:8080/chat/map?subject=an%20array%20of%20numbers%20from%201%20to%209%20under%20they%20key%20name%20'numbers'"
```

**응답:**
```json
{
  "type": "MapOutputConverter",
  "result": {
    "numbers": [1, 2, 3, 4, 5, 6, 7, 8, 9]
  }
}
```

### 4. ListOutputConverter
**GET** `/chat/list?subject={subject}`

주제에 대한 항목들을 `List<String>` 형태로 반환합니다.

**예제:**
```bash
curl "http://localhost:8080/chat/list?subject=ice%20cream%20flavors"
```

**응답:**
```json
{
  "type": "ListOutputConverter",
  "count": 5,
  "items": [
    "Vanilla",
    "Chocolate",
    "Strawberry",
    "Mint Chocolate Chip",
    "Cookie Dough"
  ]
}
```

## 💡 코드 설명

### BeanOutputConverter

#### 단일 Bean 변환
```java
ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
    .user(u -> u.text("Generate the filmography of 5 movies for {actor}.")
                .param("actor", "Tom Hanks"))
    .call()
    .entity(ActorsFilms.class);
```

#### Generic Bean Type (List<Bean>)
```java
List<ActorsFilms> actorsFilms = ChatClient.create(chatModel).prompt()
    .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
    .call()
    .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
```

### MapOutputConverter
```java
Map<String, Object> result = ChatClient.create(chatModel).prompt()
    .user(u -> u.text("Provide me a List of {subject}")
                .param("subject", "an array of numbers from 1 to 9 under they key name 'numbers'"))
    .call()
    .entity(new ParameterizedTypeReference<Map<String, Object>>() {});
```

### ListOutputConverter
```java
List<String> flavors = ChatClient.create(chatModel).prompt()
    .user(u -> u.text("List five {subject}")
                .param("subject", "ice cream flavors"))
    .call()
    .entity(new ListOutputConverter(new DefaultConversionService()));
```

## 🎨 웹 UI 사용법

1. 브라우저에서 `http://localhost:8080` 접속
2. 드롭다운 메뉴에서 원하는 OutputConverter 선택
3. 입력 필드에 값 입력
4. "조회하기" 버튼 클릭
5. 결과 확인

## 📝 주요 기능

- ✅ 단일 배우의 영화 목록 조회 (BeanOutputConverter)
- ✅ 여러 배우의 영화 목록 조회 (Generic Bean Type)
- ✅ 구조화된 데이터 조회 (MapOutputConverter)
- ✅ 간단한 목록 조회 (ListOutputConverter)
- ✅ 반응형 웹 UI
- ✅ 실시간 결과 표시
- ✅ JSON 형식으로 결과 확인

## 🔍 참고사항

- OpenAI API 사용량에 따라 비용이 발생할 수 있습니다.
- AI 응답은 매번 다를 수 있습니다.
- `application.yml`에서 모델과 파라미터를 조정할 수 있습니다.

## 📚 참고 자료

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)

## 📄 라이선스

이 프로젝트는 학습 목적으로 작성되었습니다.
