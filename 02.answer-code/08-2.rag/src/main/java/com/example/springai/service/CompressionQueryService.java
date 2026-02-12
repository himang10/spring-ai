package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

/**
 * 검색 전(Pre-Retrieval) 단계에서 CompressionQueryTransformer를 사용하는 Advanced RAG 서비스입니다.
 * CompressionQueryTransformer를 사용하여 대화 이력과 추후 질문을 하나의 독립된 질문으로 압축합니다.
 * 긴 대화 이력이 있을 때 효과적입니다.
 */
@Service
public class CompressionQueryService {
    private final ChatClient chatClient;

    /**
     * QueryTransformerService2 생성자.
     * 대화 이력 압축기(CompressionQueryTransformer)와 문서 검색기(DocumentRetriever)를 설정합니다.
     *
     * @param builder ChatClient 빌더
     * @param vectorStore 벡터 저장소
     * @param chatMemory 대화 메모리
     */
    public CompressionQueryService(ChatClient.Builder builder, VectorStore vectorStore, ChatMemory chatMemory) {
        ChatClient.Builder mutateChatClient = builder.build().mutate()
                .defaultAdvisors(new SimpleLoggerAdvisor());
                        
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(CompressionQueryTransformer.builder()
                        .chatClientBuilder(mutateChatClient)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .topK(3)
                        .similarityThreshold(0.7)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(), // Chat Memory Advisor 추가
                        advisor, 
                        new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1))
                .build();
    }

    /**
     * 질문에 대한 답변을 생성합니다. (대화 이력 압축 -> 검색 -> 답변 생성)
     *
     * @param question 사용자 질문
     * @param conversationId 대화 ID (세션 ID 등)
     * @return 생성된 답변
     */
    public String answer(String question, String conversationId) {
        return chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .call()
                .content();
    }
}
