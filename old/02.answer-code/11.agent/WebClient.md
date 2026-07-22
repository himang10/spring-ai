# Spring WebClient 완전 정리

이 문서는 이 프로젝트에서 사용하는 `WebClient` (Spring WebFlux)의 동작 방식을 질문/답변 형태로 정리한 것입니다.

---

## 1. 메서드 체인 전체 흐름

```java
String responseBody = webClient.get()         // ① HTTP GET 요청 준비
    .uri(uriBuilder -> uriBuilder             // ② URL + 쿼리 파라미터 설정
        .queryParam("engine", "youtube")
        .queryParam("search_query", query)
        .build())
    .retrieve()                               // ③ 응답 처리 모드 선언
    .onStatus(status -> status.value() == 403,// ④ 특정 상태코드 커스텀 처리 등록
        response -> ...)
    .bodyToMono(String.class)                 // ⑤ 응답 바디를 Mono로 변환 선언
    .block();                                 // ⑥ 실제 네트워크 요청 발생 + 동기 대기
```

**중요:** ①~⑤는 모두 "선언"이고 네트워크 통신이 발생하지 않습니다. `block()` 또는 `subscribe()`가 호출될 때 비로소 실제 요청이 전송됩니다.

---

## 2. 각 메서드 역할

### ① `retrieve()`

**역할:** `RequestHeadersSpec` → `ResponseSpec` 타입 전환. "바디만 처리하겠다" 선언.

```java
.retrieve()
```

- Netty에 ID를 등록하거나 요청을 전송하는 것이 **아닙니다**
- `ResponseSpec` 객체를 반환하여 `onStatus()`, `bodyToMono()` 체이닝을 가능하게 함
- 4xx/5xx 를 **자동으로 예외**로 변환하는 기본 동작 내장

```
retrieve() 기본 동작:
  2xx → 정상 처리 (바디 반환)
  4xx → WebClientResponseException.BadRequest 등 자동 throw
  5xx → WebClientResponseException.InternalServerError 등 자동 throw
```

---

### ② `onStatus()`

**역할:** 특정 HTTP 상태코드에 대해 `retrieve()` 기본 예외 처리를 **커스텀 예외로 교체(덮어쓰기)**.

```java
.onStatus(
    status -> status.value() == 403,         // 조건: 어떤 상태코드?
    response -> response.bodyToMono(String.class)
        .map(body -> new RuntimeException("SerpApi 할당량이 초과되었습니다."))
)
.onStatus(
    status -> status.value() == 429,
    response -> response.bodyToMono(String.class)
        .map(body -> new RuntimeException("SerpApi 요청 한도를 초과했습니다."))
)
```

- **선언(등록) 행위**입니다. 실행이 아님
- 등록 순서대로 상태코드를 체크
- `onStatus()`에 명시하지 않은 4xx/5xx는 `retrieve()` 기본 예외 처리가 담당

| 상태코드 | 처리 담당 |
|----------|----------|
| 403 | `onStatus` 커스텀 예외 |
| 429 | `onStatus` 커스텀 예외 |
| 나머지 4xx/5xx | `retrieve()` 기본 자동 예외 |
| 2xx | 정상 → `bodyToMono`로 전달 |

---

### ③ `bodyToMono(String.class)`

**역할:** 응답 바디를 지정 타입으로 변환하는 비동기 스트림(`Mono`) 파이프라인 구성.

```java
.bodyToMono(String.class)   // → Mono<String> 반환 (아직 실행 안 됨)
.block()                    // → 실제 실행 + 현재 스레드 블로킹 대기
```

- `Mono<String>` = "나중에 String 하나를 줄게" 라는 약속 (Cold Stream)
- `block()` 없이는 아무것도 실행되지 않음
- `block()` 은 Reactor의 비동기 파이프라인을 동기로 전환하는 브리지

---

## 3. `retrieve()` vs `exchangeToMono()` 비교

```java
// ① retrieve() : 바디만 관심, 상태코드는 자동/onStatus로 처리 (간결)
.retrieve()
.onStatus(...)              // 특정 코드만 커스텀 처리 (선택)
.bodyToMono(String.class)

// ② exchangeToMono() : 상태코드 + 헤더 + 바디 전부 직접 제어 (세밀)
.exchangeToMono(response -> {
    HttpStatusCode status = response.statusCode();
    HttpHeaders headers = response.headers().asHttpHeaders();

    if (status.value() == 403) {
        return Mono.error(new RuntimeException("할당량 초과"));
    }
    if (status.is2xxSuccessful()) {
        return response.bodyToMono(String.class);
    }
    return response.bodyToMono(String.class)
        .map(body -> "기타 오류: " + status.value());
})
```

| 구분 | `retrieve()` | `exchangeToMono()` |
|------|-------------|---------------------|
| 접근 가능 정보 | 바디만 | 상태코드 + 헤더 + 바디 전부 |
| 4xx/5xx 기본 처리 | 자동 예외 throw | 직접 처리해야 함 |
| 특정 코드 커스텀 | `onStatus()`로 일부 교체 | 모든 코드 직접 분기 |
| 코드 간결성 | 간결 | 장황하지만 세밀한 제어 |
| 사용 시점 | 일반적인 경우 | 헤더 읽기, 모든 상태코드 직접 제어 필요 시 |

---

## 4. 동기 vs 비동기 방식

### 현재 코드 (동기 방식 - `block()`)

```java
// @Tool 메서드: 동기 반환 필수
public String searchYoutubeVideos(String query) {
    String responseBody = webClient.get()
        .uri(...)
        .retrieve()
        .onStatus(...)
        .bodyToMono(String.class)
        .block();           // ← 현재 스레드를 블로킹해서 결과 대기
    ...
}
```

### 비동기 방식 (Mono 반환)

```java
// 반환 타입을 Mono<String>으로 변경
public Mono<String> searchYoutubeVideosAsync(String query) {
    return webClient.get()
        .uri(...)
        .retrieve()
        .onStatus(...)
        .bodyToMono(String.class)
        .map(responseBody -> {
            // 응답 처리를 체인 안에서 수행
            JsonNode root = objectMapper.readTree(responseBody);
            ...
            return formatVideosAsJson(videoResults);
        })
        .onErrorReturn("YouTube 검색 오류가 발생했습니다.");
        // block() 없음 → Mono를 그대로 반환
}
```

| 구분 | 동기 (`block()`) | 비동기 (`Mono` 반환) |
|------|-----------------|---------------------|
| 실행 시점 | `block()` 호출 즉시 | `subscribe()` 또는 다른 Mono에 연결될 때 |
| 호출 스레드 | 블로킹되어 대기 | 즉시 반환, 다른 스레드에서 처리 |
| Spring AI `@Tool` 호환 | ✅ 필수 사용 | ❌ 직접 사용 불가 (동기 반환값 필요) |
| Spring WebFlux Controller | 가능하나 비권장 | ✅ 권장 |

---

## 5. `subscribe()` 동작과 스레드

```java
searchYoutubeVideosAsync("서울 여행")
    .subscribe(
        result -> log.info("결과: {}", result),  // 나중에 별도 스레드에서 실행
        error  -> log.error("오류: {}", error)
    );
// subscribe() 호출 즉시 여기로 반환됨 (result 람다는 아직 실행 안 됨)
```

```
[호출 스레드]                    [Reactor Netty IO 스레드]
      |                                        |
  subscribe() 호출  ──────────────────────────►| 네트워크 요청 전송
      |                                        | 응답 대기 중...
  즉시 반환                                    |
  다음 코드 실행 중...                         | 응답 수신 완료
                                               | result 람다 실행
                                               | log.info("결과: ...")
```

- `subscribe()` 가 호출되면 `searchYoutubeVideosAsync` 메서드는 종료
- result 람다는 **Reactor Netty IO 스레드** (`reactor-http-nio-n`)에서 나중에 실행
- `subscribe()` 호출과 result 람다 실행은 **서로 다른 시점, 서로 다른 스레드**

---

## 6. `subscribe()` 위치 패턴

### 패턴 A: 호출자가 `subscribe()` 직접 호출

```java
public void someMethod() {
    searchYoutubeVideosAsync("서울 여행")
        .subscribe(                      // ← 호출자가 직접 구독
            result -> saveToDb(result),
            error  -> log.error("실패", error)
        );
}
```

### 패턴 B: Mono 체인으로 전달 (권장)

```java
// Controller가 Mono를 그대로 반환
@GetMapping("/search")
public Mono<String> search(@RequestParam String query) {
    return searchYoutubeVideosAsync(query);
    // Spring WebFlux 프레임워크가 subscribe()를 대신 호출
}
```

### 패턴 C: `block()`으로 동기화 (현재 @Tool에서 사용)

```java
String result = searchYoutubeVideosAsync(query).block();
```

---

## 7. Reactor 핵심 원칙

```
Mono 체인 흐름 (Spring WebFlux 권장):

searchYoutubeVideosAsync()  →  Mono<String>  (선언만, 네트워크 미발생)
    ↓ 반환
Controller.search()         →  Mono<String>  (그대로 전달)
    ↓ 반환
Spring WebFlux 프레임워크   →  subscribe()   (프레임워크가 최종 구독)
                                              (네트워크 요청 실제 발생)
```

> **Cold Stream 원칙:** `subscribe()` 또는 `block()`이 호출되기 전까지 Mono는 실행 계획만 갖고 있으며 **아무것도 실행되지 않습니다.**

> **subscribe는 체인 끝에서 한 번만:** 중간에서 `subscribe()`를 직접 호출하면 체인이 끊어져 에러 전파, 백프레셔(backpressure)가 정상 동작하지 않습니다.

---

## 8. `@Tool` 메서드에서 `block()` 이 필수인 이유

```
Spring AI 동작 흐름:

LLM → tool_call 응답
  → Spring AI: @Tool 메서드 실행
  → 메서드 반환값(String)을 tool_result로 LLM에 전달
  → LLM: 다음 응답 생성

Spring AI는 @Tool 메서드의 동기 반환값을 기대합니다.
→ Mono<String>을 반환하면 Spring AI가 처리 불가
→ block()으로 String을 직접 반환해야 함
```

비동기 방식은 Spring WebFlux를 완전히 채택한 Controller/Service 레이어에서 유효합니다.
