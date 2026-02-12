package com.example.springai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.InMemoryService;
import com.example.springai.service.MultiMessageService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/chat")
@Slf4j
public class InMemoryController {
  @Autowired
  private InMemoryService inMemoryService;
  
  @Autowired
  private MultiMessageService multiMessageService;
  
  @PostMapping(
    value = "/in-memory",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String inMemoryChatMemory(
      @RequestParam("question") String question, HttpSession session) {
    String answer = inMemoryService.chat(question, session.getId());
    return answer;
  }
  
  @PostMapping(
    value = "/multi-message",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String multiMessageChat(
      @RequestParam("question") String question, HttpSession session) {
    log.info("MultiMessage 대화 요청: {}", question);
    String answer = multiMessageService.chat(question, session.getId());
    return answer;
  }
}
