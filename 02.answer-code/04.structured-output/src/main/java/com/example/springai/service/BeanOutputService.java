package com.example.springai.service;

import com.example.springai.model.ActorsFilms;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeanOutputService {
    
    private final ChatClient chatClient;
    
    public BeanOutputService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    /**
     * 단일 Bean 타입으로 변환
     * 특정 배우의 영화 목록을 ActorsFilms 객체로 반환
     */
    public ActorsFilms getSingleActorFilms(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .entity(ActorsFilms.class);
    }
    
    /**
     * Generic Bean Type - List<Bean>으로 변환
     * 여러 배우의 영화 목록을 List<ActorsFilms>로 반환
     * BeanOutputConverter가 List 타입을 처리할 수 있도록 지원
     */
    public List<ActorsFilms> getMultipleActorsFilms(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
    }
}
