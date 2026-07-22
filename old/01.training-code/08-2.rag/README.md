# Spring AI RAG Playground

벡터 스토어(PGVector)에 적재된 대한민국 헌법 샘플 문서를 활용해 다양한 Retrieval Augmented Generation(RAG) 전략을 실험할 수 있는 Spring Boot 애플리케이션입니다. Spring AI 1.1.0에서 제공하는 `ChatClient`, `QuestionAnswerAdvisor`, `RetrievalAugmentationAdvisor`, `MultiQueryExpander` 등 고수준 컴포넌트를 조합해 Pre-Retrieval, Retrieval, Post-Retrieval 단계를 손쉽게 비교할 수 있습니다.

## 특징 요약

- **통합 RAG 샘플**: 기본 검색, 쿼리 재작성, 멀티 쿼리 확장, 컨텍스트 증강 파이프라인을 한 프로젝트에서 실행
- **PGVector 연동**: PostgreSQL + PGVector에 저장된 임베딩을 `VectorStoreDocumentRetriever`가 검색
- **프론트엔드 연동**: `/` 페이지에서 Strategy 드롭다운을 선택하고 질문을 바로 보낼 수 있는 간단한 UI 제공
- **풍부한 로깅**: `SimpleLoggerAdvisor`를 통해 Query Expander/Transformer 단계의 프롬프트를 콘솔에서 확인 가능

## 시스템 구성도

```
사용자 → Home UI (fetch) → Spring MVC Controller → ChatClient.prompt()
    → (선택) Query Transformer/Expander
    → VectorStoreDocumentRetriever (PGVector)
    → (선택) DocumentPostProcessor → ContextualQueryAugmenter
    → OpenAI Chat Completion → 응답 반환
```

## 서비스/엔드포인트 일람

| 구분 | Service | 핵심 컴포넌트 | 엔드포인트 |
|------|---------|---------------|------------|
| 고수준 Q&A | `QuestionAnswerService` | `QuestionAnswerAdvisor` + `SearchRequest(topK=1, similarity=0.3)` | `POST /chat/qa` |
| Retrieval Only | `RetrievalRAGService` | `RetrievalAugmentationAdvisor` + `VectorStoreDocumentRetriever(topK=3)` | `POST /chat/rag/retrieval` |
| Query Rewrite | `QueryTransformerService` | `RewriteQueryTransformer` → `VectorStoreDocumentRetriever` | `POST /chat/rag/pre-retrieval` |
| Multi-Query Expansion | `MultiQueryExpanderService` | `MultiQueryExpander(n=3)` + `VectorStoreDocumentRetriever(topK=5)` | `POST /chat/rag/multi-query-expander` |
| Post-Retrieval Pipeline | `PostRetrievalRAGService` | `MultiQueryExpander` → `VectorStoreDocumentRetriever` → Score Filter → `ContextualQueryAugmenter` | `POST /chat/rag/post-retrieval` |


## 실행 전 준비

1. **환경 변수**
   ```bash
   export OPEN_AI_KEY=sk-...
   ```

2. **PostgreSQL + PGVector 실행** (도커 사용 예)
사전에 Docker로 실행되어 있어야 한다

3. **임베딩 데이터 적재**
   - 기본으로 `src/main/resources/documents/대한민국헌법(19880225).txt` 등 샘플 문서가 포함되어 있으며, 애플리케이션 최초 실행 시 `rag_vector_store` 테이블이 생성되고 자동으로 로드됩니다.

## 애플리케이션 실행

```bash
mvn clean install -DskipTests
mvn spring-boot:run
# 또는
java -jar target/spring-ai-rag-0.0.1-SNAPSHOT.jar
```

접속: `http://localhost:8080`

## API 사용 예시

```bash
# 1. 고수준 Q&A
curl -X POST http://localhost:8080/chat/qa \
  -d "question=대한민국 헌법 제1조 1항의 내용은 무엇인가요?"

# 2. 기본 Retrieval
curl -X POST http://localhost:8080/chat/rag/retrieval \
  -d "question=대한민국 헌법 제1조 2항의 내용은 무엇인가요?"

# 3. 쿼리 재작성 전략
curl -X POST http://localhost:8080/chat/rag/pre-retrieval \
  -d "question=국회의원이 하라는 일은 하지 않고, 자기 개인 이익만 챙기고 있고 이게 국회의원이 할일이냐?"

# 4. Multi Query Expander
curl -X POST http://localhost:8080/chat/rag/multi-query-expander \
  -d "question=대통령의 임기는 어떻게 되나요?"

# 5. Post-Retrieval 파이프라인
curl -X POST http://localhost:8080/chat/rag/post-retrieval \
  -d "question=국회의 임시회의는 어떤 조건에 개최될수 있나요?"
```

응답은 `{"question": "...", "answer": "..."}` 형식의 JSON입니다.

## 주요 클래스 요약

- `QuestionAnswerService` : `QuestionAnswerAdvisor` 하나만으로 검색과 컨텍스트 주입을 모두 해결하는 가장 간단한 RAG.
- `RetrievalRAGService` : `RetrievalAugmentationAdvisor` + `VectorStoreDocumentRetriever` 조합, 파이프라인의 Retrieval 단계만 다룹니다.
- `QueryTransformerService` : `RewriteQueryTransformer` 전처리로 검색 적합도를 끌어올리는 Pre-Retrieval 전략.
- `MultiQueryExpanderService` : 하나의 질문을 3개 변형으로 확장하여 다양한 각도의 검색을 수행. `SimpleLoggerAdvisor`를 expander builder에 붙여 LLM 프롬프트를 콘솔에 출력합니다.
- `PostRetrievalRAGService` : 위 전략을 조합한 풀 파이프라인. Multi-Query → Retrieval → 점수 필터 → `ContextualQueryAugmenter` 순으로 구성됩니다.

## 설정 참고

`src/main/resources/application.yml`

```yaml
spring:
  ai:
    openai:
      api-key: ${OPEN_AI_KEY}
      embedding:
        options:
          model: text-embedding-3-small
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
          max-tokens: 2000
    vectorstore:
      pgvector:
        initialize-schema: true
        schema-name: public
        table-name: rag_vector_store
logging:
  level:
    "[org.springframework.ai]": DEBUG
```

## 웹 UI 활용

- `home.html` + `chat.js` 조합으로 간단한 챗 인터페이스 제공
- Select Box에서 전략(엔드포인트)을 고르면 JS가 해당 URL로 `fetch` 요청을 보냅니다.
- 응답이 JSON이면 pretty-print 하여 보여주며, 텍스트 응답은 그대로 노출됩니다.

## 추가 팁

- `VectorStoreDocumentRetriever`의 `topK`와 `similarityThreshold`를 조정해 검색 품질을 비교해 보세요.
- `documentPostProcessors` 람다에서 재순위화 로직을 자유롭게 실험할 수 있습니다. (예: 다른 임계값, 상위 N개만 유지 등)
- `MultiQueryExpander`는 기본적으로 3개의 변형을 생성합니다. `MultiQueryExpander.builder().n(5)`와 같이 변경하여 로그에서 차이를 확인할 수 있습니다.

## 참고 자료

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [Retrieval Augmentation Advisor 가이드](https://docs.spring.io/spring-ai/reference/rag/advisors.html)
- [PGVector 소개](https://github.com/pgvector/pgvector)
