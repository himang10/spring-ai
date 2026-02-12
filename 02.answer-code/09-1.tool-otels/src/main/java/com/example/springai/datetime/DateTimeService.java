package com.example.springai.datetime;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DateTimeService {

  private ChatClient chatClient;

  @Autowired
  private DateTimeTools dateTimeTools;


  public DateTimeService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
        .build();
  }

  // ##### LLM과 대화하는 메소드 #####
  public String chat(String question) {
    String answer = this.chatClient.prompt()
        .user(question)
        .tools(dateTimeTools)
        .advisors(new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1))
        .call()
        .content();
    return answer;
  }
}
