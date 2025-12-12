package com.example.springai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.VectorStoreService;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/chat")
@Slf4j
public class VectorStoreController {
  @Autowired
  private VectorStoreService vectorStoreService;
  
  @PostMapping(
    value = "/vector-store",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String vectorStoreChatMemory(@RequestParam("question") String question) {
    String answer = vectorStoreService.chat(question, "vector-store");
    return answer;
  }    
}
