package com.example.springai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springai.service.DefaultChatMemoryService;
import com.example.springai.service.MessageChatMemoryService;
import com.example.springai.service.ManualChatMemoryService;
import com.example.springai.service.PromptChatMemoryService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/chat")
@Slf4j
public class InMemoryController {
  @Autowired
  private MessageChatMemoryService messageWindowChatMemoryService;

  @Autowired
  private DefaultChatMemoryService inMemoryService;
  
  @Autowired
  private ManualChatMemoryService manualChatMemoryService;
  
  @Autowired
  private PromptChatMemoryService promptChatMemoryService;
  
  // In-Memory Chat Memory: 세션 기반으로 간단히 메모리를 구현한 예시
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

  // Message Window Chat Memory: 최근 N개의 메시지만 유지하는 윈도우 방식 메모리
  @PostMapping(
    value = "/message-chat-memory",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String messageChatMemory(
      @RequestParam("question") String question, HttpSession session) {
    log.info("MessageChatMemory 대화 요청: {}", question);
    String answer = messageWindowChatMemoryService.chat(question, session.getId());
    return answer;
  }

  // Prompt Chat Memory: 시스템 프롬프트에 텍스트로 메모리 추가
  @PostMapping(
    value = "/prompt-chat-memory",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String promptChatMemory(
      @RequestParam("question") String question, HttpSession session) {
    log.info("PromptChatMemory 대화 요청: {}", question);
    String answer = promptChatMemoryService.chat(question, session.getId());
    return answer;
  }

  // Manual Chat Memory: 시스템 메시지 + 사용자 메시지 + 어시스턴트 메시지를 직접 ChatMemory에 추가하여 관리
  @PostMapping(
    value = "/manual-chat-memory",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String manualChatMemory(
      @RequestParam("question") String question, HttpSession session) {
    log.info("ManualChatMemory 대화 요청: {}", question);
    String answer = manualChatMemoryService.chat(question, session.getId());
    return answer;
  }
}
