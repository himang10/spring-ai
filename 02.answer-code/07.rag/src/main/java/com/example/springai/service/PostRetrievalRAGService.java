package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 검색 후(Post-Retrieval) 또는 생성(Generation) 단계에서 컨텍스트를 보강하는 RAG 서비스입니다.
 * ContextualQueryAugmenter를 사용하여 검색된 문서를 프롬프트에 통합하는 방식을 제어합니다.
 */
@Service
public class PostRetrievalRAGService {

    private final ChatClient chatClient;
    public PostRetrievalRAGService(ChatClient.Builder builder, VectorStore vectorStore) {

        // 1. Pre-Retrieval: Query Expander
        ChatClient.Builder mutateChatClient = builder.build().mutate()
                .defaultAdvisors(new SimpleLoggerAdvisor());

        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(mutateChatClient)
                .build();

        // 2. Retrieval: Document Retriever
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.3)
                .vectorStore(vectorStore)
                .build();

        // 3. Post-Retrieval & 4. Generation
        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();

        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryExpander(queryExpander)
                .documentRetriever(documentRetriever)
                .documentPostProcessors((Query query, List<Document> documents) -> {
                    // 예시: 유사도 점수(Score)가 0.5 이상인 문서만 필터링
                    return documents.stream()
                            .filter(doc -> doc.getScore() != null && doc.getScore() >= 0.5)
                            .collect(Collectors.toList());
                })
                .queryAugmenter(queryAugmenter)
                .build();

        this.chatClient = builder
                .defaultAdvisors(advisor, new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE))
                .build();
    
    }

    /**
     * 질문에 대한 답변을 생성합니다. (검색 -> 컨텍스트 증강 -> 답변 생성)
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
