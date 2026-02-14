package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * 기본 검색(Retrieval) 단계만 포함하는 Naive RAG 서비스입니다.
 * VectorStore에서 관련 문서를 검색하여 답변 생성에 활용합니다.
 */
@Service
public class RetrievalRAGService {

    private final ChatClient chatClient;

    /**
     * RetrievalRAGService 생성자.
     * RetrievalAugmentationAdvisor를 사용하여 문서 검색기(DocumentRetriever)를 설정합니다.
     *
     * @param chatModel ChatModel
     * @param vectorStore 벡터 저장소
     */
    public RetrievalRAGService(ChatModel chatModel, VectorStore vectorStore) {
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .topK(3)
                        .similarityThreshold(0.3)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(advisor, new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * 질문에 대한 답변을 생성합니다.
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
