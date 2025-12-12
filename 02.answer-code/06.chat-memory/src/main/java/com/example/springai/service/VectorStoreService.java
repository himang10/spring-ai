package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VectorStoreService {
  private ChatClient chatClient;

  public VectorStoreService(
      VectorStore vectorStore,
      ChatClient.Builder chatClientBuilder) {

    this.chatClient = chatClientBuilder
        .defaultAdvisors(
            VectorStoreChatMemoryAdvisor.builder(vectorStore).build(),
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
          )
        .build();

  }

  public String chat(String userText, String conversationId) {
    String answer = chatClient.prompt()
        .user(userText)
        .advisors(advisorSpec -> advisorSpec.param(
            ChatMemory.CONVERSATION_ID, conversationId))
        .call()
        .content();
    return answer;
  }
}
