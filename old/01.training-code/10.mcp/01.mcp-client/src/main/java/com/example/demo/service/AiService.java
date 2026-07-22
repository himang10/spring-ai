package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;


import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService {
  // ##### 필드 #####
  private ChatClient chatClient;

  // ##### 생성자 #####
  public AiService(
                    ChatClient.Builder chatClientBuilder, 
                    ToolCallbackProvider toolCallbackProvider,
                    ChatMemory chatMemory) {

    this.chatClient = chatClientBuilder
      .defaultToolCallbacks(toolCallbackProvider)
      .defaultAdvisors (
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1))
      .build();
  } 
  
  // ##### LLM과 텍스트로 대화하는 메소드 #####
  public String chat(String question, String conversationId) {
    
    String answer = this.chatClient.prompt()
        .system("""
            HTML과 CSS를 사용해서 들여쓰기가 된 답변을 출력하세요.
            <div>에 들어가는 내용으로만 답변을 주세요. <h1>, <h2>, <h3>태그는 사용하지 마세요.
            kubernetes, metrics, log, tracing 관련된 질문은 반드시 도구를 사용해서 답변하세요.
            """)
        .user(question)
        .advisors(advisorSpec -> advisorSpec.param(
            ChatMemory.CONVERSATION_ID, conversationId            
        ))
        .call()
        .content();
    return answer;
  }
}
