package com.example.springai.service;

import java.util.List;

import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.model.ResultMetadata;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirstEmbeddingService {

  private final EmbeddingModel embeddingModel;

  FirstEmbeddingService(EmbeddingModel embeddingModel) {
    this.embeddingModel = embeddingModel;
  }


  public void embedding(String question) {
    
    // 1. EmbeddingRequest 구성
    EmbeddingOptions options = EmbeddingOptions.builder()
        .build();
    
    EmbeddingRequest request = new EmbeddingRequest(
        List.of(question), 
        options
    );
  
    
    // 2. EmbeddingResponse 받기
    EmbeddingResponse response = embeddingModel.call(request);

    // 임베딩 모델 정보 얻기
    EmbeddingResponseMetadata metadata = response.getMetadata();
    System.out.println("############################");
    log.info("모델 이름: {}", metadata.getModel());
    log.info("모델의 임베딩 차원: {}", embeddingModel.dimensions());
    log.info("사용된 토큰 수: {}", metadata.getUsage().getTotalTokens());

    // 임베딩 결과 얻기
    Embedding embedding = response.getResults().get(0);
    log.info("벡터 차원: {}", embedding.getOutput().length);
    log.info("벡터: {}", embedding.getOutput());
    System.out.println("############################");
    
  }

}
