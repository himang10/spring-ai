package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
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
     * @param chatModel ChatModel
     * @param vectorStore 벡터 저장소
     * @param chatMemory 대화 메모리
     */

// ...

    public CompressionQueryService(ChatModel chatModel, VectorStore vectorStore, ChatMemory chatMemory) {
        ChatClient.Builder transformerBuilder = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor());

        String compressionPrompt = """
                당신은 대화 이력과 후속 질문을 바탕으로 검색에 최적화된 독립적인 질문을 생성하는 전문가입니다.
                
                대화 이력:
                {history}
                
                후속 질문:
                {query}
                
                위 내용을 바탕으로, 이전 대화의 문맥을 포함하여 명확하고 구체적인 하나의 독립된 질문으로 다시 작성해주세요.
                답변은 질문 내용만 포함해야 합니다.
                """;
                        
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(
                        // 단계 1: 질문을 재작성하여 검색 최적화
                        RewriteQueryTransformer.builder()
                                .chatClientBuilder(transformerBuilder)
                                .build(),
                        // 단계 2: 대화 이력을 압축하여 독립된 질문으로 변환
                        CompressionQueryTransformer.builder()
                                .chatClientBuilder(transformerBuilder)
                                .promptTemplate(new PromptTemplate(compressionPrompt))
                                .build()
                )
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .topK(3)
                        .similarityThreshold(0.3)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        this.chatClient = ChatClient.builder(chatModel)
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
