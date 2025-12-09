package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.springai.advisor.AdvisorA;
import com.example.springai.advisor.AdvisorB;
import com.example.springai.advisor.AdvisorC;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BasicService {
  private ChatClient chatClient;

  public BasicService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
        .defaultAdvisors(
            new AdvisorA(),
            new AdvisorB())
        .build();
  }

  public String callAdvisor(String question) {
    String response = chatClient.prompt()
        .advisors(new AdvisorC())
        .user(question)
        .call()
        .content();
    return response;
  }
}
