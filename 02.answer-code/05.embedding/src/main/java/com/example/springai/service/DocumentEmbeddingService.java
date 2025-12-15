package com.example.springai.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DocumentEmbeddingService {
  private final VectorStore vectorStore;

  DocumentEmbeddingService( VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }


  public void addDocuments() {
    log.info("addDocuments() called");
    // Document 목록 생성
    List<Document> documents = List.of(
        new Document("spring은 web application을 쉽게 만들기 위한 프레임워크입니다.", Map.of("category", "spring", "year", 1987)),
        new Document("spring ai는 AI 모델과의 통합을 단순화하는 프레임워크입니다.", Map.of("category", "spring", "year", 2021)),
        new Document("vector Store는 임베딩 벡터를 저장하고 유사도 검색을 수행합니다.", Map.of("category", "spring", "year", 2021)),
        new Document("ChatClient는 대화형 AI 모델과 상호작용하는 인터페이스입니다.", Map.of("category", "spring", "year", 2022)),
        new Document("embedding 모델은 텍스트를 고차원 벡터로 변환합니다.", Map.of("category", "spring", "year", 2022)),
        new Document("spring Boot는 독립 실행형 프로덕션 급 애플리케이션을 쉽게 만듭니다.", Map.of("category", "spring", "year", 1999)),
        new Document("Docker 컨테이너는 애플리케이션을 격리된 환경에서 실행합니다.", Map.of("category", "cloud", "year", 2024)),
        new Document("kubernetes는 컨테이너화된 애플리케이션의 배포와 관리를 자동화합니다.", Map.of("category", "cloud", "year", 2025)));

    // 벡터 저장소에 저장
    vectorStore.add(documents);
  }
  
  public List<Document> search(String question) {
    log.info("search() question: {}", question);
    // List<Document> documents = vectorStore.similaritySearch(question);
    List<Document> documents = vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(question)
            .topK(1)
            .build());
    return documents;
  }

  public List<Document> searchWithFilter(String question) {
    log.info("searchWithFilter() question: {}", question);
    List<Document> documents = vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(question)
            .topK(1)
            .similarityThreshold(0.3)
            .filterExpression("category == 'cloud' && year >= 2020")
            .build());
    return documents;
  }

  public void deleteDocumentForSimilarity(String request) {
    log.info("deleteDocumentForSimilarity() request: {}", request);
    // request 내용과 유사한 문서를 찾아서 삭제
    List<Document> similarDocuments = vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(request)
            .topK(1)
            .build());
    
    if (!similarDocuments.isEmpty()) {
      Document docToDelete = similarDocuments.get(0);
      String id = docToDelete.getId();
      vectorStore.delete(List.of(id));
      log.info("Deleted document with ID: {}", id);
    } else {
      log.info("No matching document found to delete");
    }
  }

  public void deleteDocuments() {
    log.info("deleteDocuments() called");
    vectorStore.delete("year >= 1987");
  }
}
