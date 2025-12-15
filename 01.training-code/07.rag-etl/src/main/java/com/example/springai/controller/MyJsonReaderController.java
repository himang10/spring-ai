package com.example.springai.controller;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.MyJsonReaderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Slf4j
public class MyJsonReaderController {  

  @Autowired
  private MyJsonReaderService myJsonReaderService;
  
  @PostMapping(
    value = "/json",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String jsonReader(@RequestParam("question") String question) {
    log.info("JSON Reader - Question: {}", question);
    
    try {
      List<Document> documents = myJsonReaderService.loadJsonAsDocuments();
      log.info("Loaded {} documents from JSON", documents.size());
      
      String result = myJsonReaderService.documentsToString(documents);
      log.info("Search result: {}", result);
      
      return "문서 로딩 완료: " + documents.size() + "개의 문서를 찾았습니다.\n\n" + result;
    } catch (Exception e) {
      log.error("Error loading JSON documents", e);
      return "오류 발생: " + e.getMessage();
    }
  }

  @PostMapping(
    value = "/json-etl",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String jsonReaderWithETL(@RequestParam("question") String question) {
    log.info("JSON ETL Pipeline - Question: {}", question);

    try {
      myJsonReaderService.executeETL();
      return "ETL 파이프라인 완료: JSON 문서를 읽고, 변환하고, PGVector에 저장했습니다.\n이제 벡터 저장소에서 문서를 검색할 수 있습니다.";
    } catch (Exception e) {
      log.error("Error executing ETL pipeline", e);
      return "ETL 파이프라인 오류 발생: " + e.getMessage();
    }
  }
}