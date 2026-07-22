package com.example.springai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.FirstEmbeddingService;
import com.example.springai.service.TextEmbeddingService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@Slf4j
public class FirstEmbeddingController {
  @Autowired
  private FirstEmbeddingService firstEmbeddingService;

  
  // ##### 요청 매핑 메소드 #####
  @PostMapping(
      value = "/first-embedding",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String firstEmbedding(@RequestParam("question") String question) {
    firstEmbeddingService.embedding(question);
    return "서버 터미널(콘솔) 출력을 확인하세요.";
  }  
}
