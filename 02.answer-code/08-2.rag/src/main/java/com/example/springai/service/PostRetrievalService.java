package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
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
public class PostRetrievalService {

    private final ChatClient chatClient;
    public PostRetrievalService(ChatModel chatModel, VectorStore vectorStore) {

        // 1. Pre-Retrieval를 위한 ChatClient Builder 생성
        ChatClient.Builder preRetrievalBuilder = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor());

        // 2. Pre-Retrieval: Query Expander
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(preRetrievalBuilder)
                .build();

        // 3. Retrieval: Document Retriever
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.3)
                .vectorStore(vectorStore)
                .build();

        // 4. Post-Retrieval: Document Post-Processor
        // 현재 구현: 점수 기반 필터링만 적용
        DocumentPostProcessor documentPostProcessor = (Query query, List<Document> documents) -> {
            // 예시: 유사도 점수(Score)가 0.5 이상인 문서만 필터링
            return documents.stream()
                    .filter(doc -> doc.getScore() != null && doc.getScore() >= 0.5)
                    .collect(Collectors.toList());
        };

        // 5. Generation: 한글 프롬프트로 컨텍스트 증강
        String contextPrompt = """
                아래는 관련 컨텍스트 정보입니다.
                
                ---------------------
                {context}
                ---------------------
                
                주어진 컨텍스트 정보만을 사용하여 질문에 답변하세요.
                
                다음 규칙을 따르세요:
                
                1. 컨텍스트에 답변이 없으면 "제공된 정보에서 답변을 찾을 수 없습니다"라고 말하세요.
                2. "컨텍스트에 따르면..." 또는 "제공된 정보에 의하면..."과 같은 표현은 피하세요.
                
                질문: {query}
                
                답변:
                """;
        
        String emptyContextPrompt = """
                사용자의 질문이 제공된 지식 범위를 벗어났습니다.
                정중하게 답변할 수 없다고 안내해주세요.
                """;
        
        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(new PromptTemplate(contextPrompt))
                .emptyContextPromptTemplate(new PromptTemplate(emptyContextPrompt))
                .allowEmptyContext(false)
                .build();

        // 6. Retrieval Augmentation Advisor 설정
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryExpander(queryExpander)
                .documentRetriever(documentRetriever)
                .documentPostProcessors(documentPostProcessor)
                .queryAugmenter(queryAugmenter)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
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
