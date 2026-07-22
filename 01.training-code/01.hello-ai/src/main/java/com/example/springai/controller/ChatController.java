package com.example.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 동기(call)와 비동기(stream) 방식을 모두 지원하는 ChatClient 예제 Controller.
 *
 * - GET /ai        : 동기 방식. 응답이 모두 생성된 후 한번에 반환한다.
 * - GET /ai/stream : 비동기(스트리밍) 방식. 토큰이 생성되는 대로 순차적으로 반환한다.
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    // Autoconfigured ChatClient.Builder is injected
    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /** ------------------------------------------------------------
     * 아래에 chat() 메서드를 구현하세요
     * ChatClient Fluent API를 사용하여 동기 방식으로 AI 응답을 생성합니다.
     * ------------------------------------------------------------ */
    @GetMapping("/ai")
    public String chat(@RequestParam String userInput) {
    }

    /** ------------------------------------------------------------
     * 아래에 chatStream() 메서드를 구현하세요
     * ChatClient Fluent API를 사용하여 비동기(스트리밍) 방식으로 AI 응답을 생성합니다.
     * ------------------------------------------------------------ */
    @GetMapping(value = "/ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String userInput) {
    }
}
