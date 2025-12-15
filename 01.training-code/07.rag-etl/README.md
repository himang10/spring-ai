# Spring AI RAG 문서 Reader & ETL Pipeline 가이드

이 프로젝트는 다양한 문서 형식을 읽고, Spring AI ETL 파이프라인을 통해 PGVector에 저장하는 완전한 RAG(Retrieval Augmented Generation) 시스템입니다.

## 주요 기능

1. **문서 읽기**: JSON, TXT, HTML, PDF, DOCX, URL 지원
2. **ETL 파이프라인**: Extract → Transform (TokenTextSplitter) → Load (PGVector)
3. **웹 UI**: 각 문서 형식별 일반 읽기 / ETL 파이프라인 선택 가능
4. **벡터 검색**: 저장된 문서를 벡터 기반으로 검색

## Service 목록 및 기능

각 Service는 두 가지 주요 기능을 제공합니다:
- 문서 읽기 (`load*AsDocuments()`)
- ETL 파이프라인 (`executeETL()`)

| Service | 읽기 메소드 | ETL 메소드 | 설명 |
|---------|------------|-----------|------|
| `MyJsonReaderService` | `loadJsonAsDocuments()` | `executeETL()` | JSON 파일 처리 |
| `MyTxtReaderService` | `loadTxtAsDocuments()` | `executeETL()` | 텍스트 파일 처리 |
| `MyHtmlReaderService` | `loadHtmlAsDocuments()` | `executeETL()` | HTML 파일 처리 |
| `MyPdfReaderService` | `loadPdfAsDocuments()` | `executeETL()` | PDF 파일 처리 |
| `MyDocxReaderService` | `loadDocxAsDocuments()` | `executeETL()` | DOCX 파일 처리 |
| `MyUrlReaderService` | `loadUrlAsDocuments(url)` | `executeETL(url)` | 웹 URL 콘텐츠 처리 |

## ETL 파이프라인 아키텍처

모든 Service는 Spring AI ETL 패턴을 따릅니다:

```
Extract (Read) → Transform (Split) → Load (VectorStore)
     ↓              ↓                    ↓
DocumentReader → TokenTextSplitter → PGVector
```

### ETL 구성 요소

1. **Extract (추출)**
   - `DocumentReader` 구현체 사용 (JsonReader, TextReader, TikaDocumentReader 등)
   - 문서를 `List<Document>`로 변환

2. **Transform (변환)**
   - `TokenTextSplitter`: 큰 문서를 AI 모델 컨텍스트 윈도우에 맞게 분할
   - CL100K_BASE 인코딩 (OpenAI 모델 호환)
   - 기본 설정: 800 토큰/청크, 최소 350자

3. **Load (로드)**
   - `VectorStore`: PGVector에 임베딩하여 저장
   - 자동으로 벡터 변환 및 인덱싱

## Service별 Reader와 사용법

| Service | 내부 사용 Reader | 주요 설정/특징 | 비고 |
|---|---|---|---|
| `MyJsonReaderService` | `JsonReader` | JSON 필드 `source`, `content` 추출 | Spring AI 내장 |
| `MyTxtReaderService` | `TextReader` | UTF-8, 메타데이터 추가 | Spring AI 내장 |
| `MyHtmlReaderService` | `Jsoup` | HTML 파싱, 텍스트 추출 | JsoupDocumentReader 미지원 시 대체 |
| `MyPdfReaderService` | `TikaDocumentReader` | PDF 텍스트 추출 | Apache Tika 기반 |
| `MyDocxReaderService` | `TikaDocumentReader` | DOCX 텍스트 추출 | Apache Tika 기반 |
| `MyUrlReaderService` | `TikaDocumentReader` | URL 콘텐츠 자동 판별 | 동적 리소스 |

## Service별 의존성(pom.xml)

아래는 “지금 추가한 Service”를 기준으로, 어떤 의존성이 필요한지 매핑한 표입니다.

| Service | 파싱 대상 | 추가 의존성 | 비고 |
|---|---|---|---|
| `MyJsonReaderService` | `.json` | (추가 없음) | Spring AI `JsonReader` 사용 |
| `MyTxtReaderService` | `.txt` | (추가 없음) | JDK IO로 텍스트 로드 |
| `MyHtmlReaderService` | `.html` | `org.jsoup:jsoup` | HTML → 본문 텍스트 추출 |
| `MyPdfReaderService` | `.pdf` | `org.apache.tika:tika-core`, `org.apache.tika:tika-parsers-standard-package` | PDF → 텍스트 추출 |
| `MyDocxReaderService` | `.docx` | `org.apache.tika:tika-core`, `org.apache.tika:tika-parsers-standard-package` | DOCX → 텍스트 추출 |
| `MyUrlReaderService` | URL | `org.jsoup:jsoup`, `org.apache.tika:*` | HTML은 jsoup, 그 외는 tika로 파싱 |

### pom.xml에 추가된 항목

```xml
<!-- Spring AI Document Readers -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-tika-document-reader</artifactId>
</dependency>

<!-- Document parsing (txt/html/pdf/docx) -->
<!-- jsoup: HTML 파싱/텍스트 추출용 -->
<dependency>
  <groupId>org.jsoup</groupId>
  <artifactId>jsoup</artifactId>
  <version>1.18.3</version>
</dependency>

<!-- Apache Tika: 바이너리 문서에서 텍스트 추출 (PDF, DOCX 등) -->
<dependency>
  <groupId>org.apache.tika</groupId>
  <artifactId>tika-core</artifactId>
  <version>3.2.3</version>
</dependency>

<dependency>
  <groupId>org.apache.tika</groupId>
  <artifactId>tika-parsers-standard-package</artifactId>
  <version>3.2.3</version>
</dependency>

<!-- Vector Store: PGVector -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
</dependency>
```

## 리소스 배치 규칙

- 기본 파일들은 `src/main/resources/documents/` 아래에 둡니다.
- 각 Service의 `@Value("classpath:...")` 경로와 파일명이 일치해야 합니다.

현재 예제 기준:

- JSON: `documents/대한민국헌법(19880225).json`
- TXT: `documents/대한민국헌법(19880225).txt`
- HTML: `documents/대한민국헌법(19880225).html`
- PDF: `documents/sample.pdf` (직접 추가하거나 경로 수정 필요)
- DOCX: `documents/sample.docx` (직접 추가하거나 경로 수정 필요)

## 코딩 방법(사용 예시)

### 1. 문서 읽기 (Extract Only)

간단하게 문서를 읽어서 텍스트를 확인하는 경우:

```java
@Autowired MyTxtReaderService txtReader;

// 문서 읽기
List<Document> docs = txtReader.loadTxtAsDocuments();

// 문자열로 변환
String text = txtReader.documentsToString(docs);
```

### 2. ETL 파이프라인 실행

문서를 읽고, 청크로 분할하고, PGVector에 저장:

```java
@Autowired MyTxtReaderService txtReader;

// Extract → Transform → Load
txtReader.executeETL();
// 이제 PGVector에 저장되어 벡터 검색 가능
```

### 3. URL 기반 ETL

```java
@Autowired MyUrlReaderService urlReader;

// 웹 페이지를 읽고 ETL 파이프라인 실행
urlReader.executeETL("https://docs.spring.io/spring-ai/reference/");
```

### 4. 모든 Service의 공통 메소드

모든 Reader Service는 다음 메소드를 제공합니다:

```java
// 1. 문서 로드
List<Document> documents = service.load*AsDocuments();

// 2. Document 리스트를 문자열로 변환
String text = service.documentsToString(documents);

// 3. ETL 파이프라인 실행 (PGVector에 저장)
service.executeETL();  // URL 제외
// 또는
urlService.executeETL(url);  // URL만 해당
```

### 5. Controller 사용 예시

REST API를 통한 사용:

```bash
# 문서 읽기만
curl -X POST http://localhost:8080/chat/txt \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "question=test"

# ETL 파이프라인 실행 (PGVector에 저장)
curl -X POST http://localhost:8080/chat/txt-etl \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "question=test"
```

## 웹 UI 사용법

1. 애플리케이션 실행:
```bash
mvn clean install -DskipTests
java -jar target/spring-ai-rag-0.0.1-SNAPSHOT.jar
```

2. 브라우저에서 `http://localhost:8080` 접속

3. RAG 선택 드롭다운에서 옵션 선택:
   - **일반 Reader**: 문서를 읽어서 바로 표시
   - **ETL to PGVector**: 문서를 읽고 → 청크 분할 → PGVector에 저장

4. 메시지 입력 후 전송

### 사용 가능한 엔드포인트

| 엔드포인트 | 설명 |
|-----------|------|
| `/chat/json` | JSON 문서 읽기 |
| `/chat/json-etl` | JSON ETL 파이프라인 |
| `/chat/txt` | 텍스트 문서 읽기 |
| `/chat/txt-etl` | 텍스트 ETL 파이프라인 |
| `/chat/html` | HTML 문서 읽기 |
| `/chat/html-etl` | HTML ETL 파이프라인 |
| `/chat/pdf` | PDF 문서 읽기 |
| `/chat/pdf-etl` | PDF ETL 파이프라인 |
| `/chat/docx` | DOCX 문서 읽기 |
| `/chat/docx-etl` | DOCX ETL 파이프라인 |
| `/chat/url` | URL 콘텐츠 읽기 |
| `/chat/url-etl` | URL ETL 파이프라인 |

## TokenTextSplitter 설정

기본 설정값:
- **defaultChunkSize**: 800 토큰 (각 청크의 목표 크기)
- **minChunkSizeChars**: 350자 (최소 문자 수)
- **minChunkLengthToEmbed**: 5자 (임베딩할 최소 길이)
- **maxNumChunks**: 10000개 (메모리 보호)
- **keepSeparator**: true (줄바꿈 등 구분자 유지)

커스텀 설정이 필요한 경우 Service에서 파라미터를 조정할 수 있습니다.

## PGVector 설정

`application.yml`에서 PostgreSQL 및 PGVector 설정:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
  
  ai:
    vectorstore:
      pgvector:
        initialize-schema: true
        schema-name: public
        table-name: chat_memory_vector_store
```

## 주의사항

- 모든 파일 기반 Reader는 `src/main/resources/documents/` 아래에 파일이 있어야 합니다
- PDF/DOCX 파일은 직접 추가하거나 `@Value` 경로를 수정해야 합니다
- URL Reader는 실시간으로 웹 콘텐츠를 가져오므로 네트워크 연결이 필요합니다
- ETL 파이프라인 실행 시 OpenAI API 키가 필요합니다 (임베딩 생성용)
- 대용량 문서는 TokenTextSplitter에 의해 자동으로 분할됩니다

## 참고 자료

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [ETL Pipeline 가이드](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html)
- [Vector Store 문서](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)
