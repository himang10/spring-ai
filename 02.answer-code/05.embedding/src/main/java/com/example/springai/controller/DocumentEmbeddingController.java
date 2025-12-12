package com.example.springai.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.DocumentEmbeddingService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Slf4j
public class DocumentEmbeddingController {
  @Autowired
  private DocumentEmbeddingService documentEmbeddingService;

  
  // ##### 요청 매핑 메소드 #####
  
  // 문서 추가
  @PostMapping(
      value = "/add-documents",
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String addDocuments() {
    documentEmbeddingService.addDocuments();
    return "문서가 Vector Store에 추가되었습니다.";
  }

  // 유사도 검색
  @PostMapping(
      value = "/search",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<Document> search(@RequestParam("question") String question) {
    log.info("Search question: {}", question);
    List<Document> results = documentEmbeddingService.search(question);
    return results;
  }

  // 필터 적용 검색
  @PostMapping(
      value = "/search-with-filter",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<Document> searchWithFilter(@RequestParam("question") String question) {
    log.info("Search with filter question: {}", question);
    List<Document> results = documentEmbeddingService.searchWithFilter(question);
    return results;
  }

  // 유사 문서 삭제
  @PostMapping(
      value = "/delete-document-for-similarity",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String deleteDocumentForSimilarity(@RequestParam("request") String request) {
    log.info("Delete request: {}", request);
    documentEmbeddingService.deleteDocumentForSimilarity(request);
    return "유사한 문서 삭제 처리가 완료되었습니다. 로그를 확인하세요.";
  }

  // 전체 문서 삭제
  @PostMapping(
      value = "/delete-all-documents",
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String deleteAllDocuments() {
    documentEmbeddingService.deleteDocuments();
    return "모든 문서가 삭제되었습니다.";
  }
}
