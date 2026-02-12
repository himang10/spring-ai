package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * 검색 전(Pre-Retrieval) 단계에서 쿼리 변환을 수행하는 Advanced RAG 서비스입니다.
 * RewriteQueryTransformer를 사용하여 사용자 질문을 검색에 더 적합한 형태로 재작성합니다.
 */
@Service
public class RewriteQueryService {

    private final ChatClient chatClient;

    /**
     * QueryTransformerService 생성자.
     * 쿼리 변환기(QueryTransformer)와 문서 검색기(DocumentRetriever)를 설정합니다.
     *
     * @param builder ChatClient 빌더
     * @param vectorStore 벡터 저장소
     */
    public RewriteQueryService(ChatClient.Builder builder, VectorStore vectorStore) {
        ChatClient.Builder mutateChatClient = builder.build().mutate()
                .defaultAdvisors(new SimpleLoggerAdvisor());

        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(mutateChatClient)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.3)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        this.chatClient = builder
                .defaultAdvisors(advisor, new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * 질문에 대한 답변을 생성합니다. (쿼리 재작성 -> 검색 -> 답변 생성)
     *
     * @param question 사용자 질문
     * @return 생성된 답변
     */
    public String answer(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
