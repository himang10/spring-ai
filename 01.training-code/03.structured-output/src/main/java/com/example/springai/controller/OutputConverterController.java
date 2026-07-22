package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
public class OutputConverterController {

    private static record ActorsFilms (String actor, List<String> films) {}
    private final ChatClient chatClient;

    // Autoconfigured ChatClient.Builder is injected
    public OutputConverterController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    /**
     * ---------------------------------------------
     * BeanOutputConverter - 단일 Bean 메소드를 작성해보자
     * GET /ai/bean
     * return ActorsFilms
     * ---------------------------------------------
     */

    
    /**
     * ---------------------------------------------
     * BeanOutputConverter - Generic Bean Type (List<Bean>)
     * GET /ai/list-bean
     * return List<ActorsFilms>
     * ---------------------------------------------
     */

    /**
     * ---------------------------------------------
     * MapOutputConverter
     * GET /ai/map
     * return Map<String, Object>
     * ---------------------------------------------
     */
    
    /**
     * ---------------------------------------------
     * ListOutputConverter
     * GET /ai/list
     * return List<String>
     * ---------------------------------------------
     */
    @GetMapping("/list")
    public List<String> getListResult(@RequestParam String userInput) {
        return chatClient.prompt()
                .user(userInput)
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }
}
