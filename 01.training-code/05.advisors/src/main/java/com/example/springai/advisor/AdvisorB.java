package com.example.springai.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

public class AdvisorB implements CallAdvisor {
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 2;
  }

  @Override
  public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
    System.out.println("[전처리] AdvisorB");
    
    // Context에 데이터 추가
    String additionalInfo = "AdvisorB에서 추가한 정보";
    System.out.println("  - AdvisorB: Context에 데이터 추가 = " + additionalInfo);
    
    // 기존 context를 복사하고 새 데이터 추가
    Map<String, Object> newContext = new HashMap<>(request.context());
    newContext.put("advisorB_timestamp", System.currentTimeMillis());
    newContext.put("advisorB_info", additionalInfo);
    
    // 새로운 context로 request 생성
    ChatClientRequest mutatedRequest = request.mutate()
        .context(newContext)
        .build();
    
    ChatClientResponse response = chain.nextCall(mutatedRequest);
    System.out.println("[후처리] AdvisorB");
    return response;
  }
}
