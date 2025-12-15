package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

/**
 * 검색 전(Pre-Retrieval) 단계에서 CompressionQueryTransformer와 RewriteQueryTransformer를 함께 사용하는 Advanced RAG 서비스입니다.
 * 먼저 대화 이력을 압축한 후, 압축된 질문을 재작성하여 검색 성능을 극대화합니다.
 * 긴 대화 문맥에서 더욱 정확한 검색이 필요한 경우에 효과적입니다.
 */
@Service
public class QueryTransformerService3 {

    private final ChatClient chatClient;

    /**
     * QueryTransformerService3 생성자.
     * CompressionQueryTransformer와 RewriteQueryTransformer를 순차적으로 적용하고,
     * 문서 검색기(DocumentRetriever)를 설정합니다.
     *
     * @param builder ChatClient 빌더
     * @param vectorStore 벡터 저장소
     * @param chatMemory 대화 메모리
     */
    public QueryTransformerService3(ChatClient.Builder builder, VectorStore vectorStore, ChatMemory chatMemory) {
        ChatClient.Builder mutateChatClient = builder.build().mutate()
                .defaultAdvisors(new SimpleLoggerAdvisor());

        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                // 단계 1: 질문을 재작성하여 검색 최적화
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(mutateChatClient)
                        .build())
                // 단계 2: 대화 이력을 압축하여 독립된 질문으로 변환
                .queryTransformers(CompressionQueryTransformer.builder()
                        .chatClientBuilder(mutateChatClient)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .topK(3)
                        .similarityThreshold(0.3)
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
     * 질문에 대한 답변을 생성합니다.
     * (대화 이력 압축 -> 질문 재작성 -> 검색 -> 답변 생성)
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
