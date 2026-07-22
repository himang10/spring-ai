package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

/**
 * 검색 전(Pre-Retrieval) 단계에서 쿼리 확장을 수행하는 Advanced RAG 서비스입니다.
 * MultiQueryExpander를 사용하여 하나의 질문을 다양한 관점의 여러 질문으로 확장하여 검색 범위를 넓힙니다.
 */
@Service
public class MultiQueryExpanderService {

    private final ChatClient chatClient;

    /**
     * MultiQueryExpanderService 생성자.
     * 쿼리 확장기(MultiQueryExpander)와 문서 검색기(DocumentRetriever)를 설정합니다.
     *
     * @param chatModel ChatModel
     * @param vectorStore 벡터 저장소
     */
    public MultiQueryExpanderService(ChatModel chatModel, VectorStore vectorStore) {
        ChatClient.Builder expanderBuilder = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor());

        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(expanderBuilder)
                        .numberOfQueries(3)  // default=3
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .topK(5)
                        .similarityThreshold(0.3)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(advisor, new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE))
                .build();
    }

    /**
     * 질문에 대한 답변을 생성합니다. (쿼리 확장 -> 다중 검색 -> 답변 생성)
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
