package com.example.springai.controller;

import com.example.springai.service.MultiQueryExpanderService;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class MultiQueryExpanderController {

    private final MultiQueryExpanderService multiQueryExpanderService;

    public MultiQueryExpanderController(MultiQueryExpanderService multiQueryExpanderService) {
        this.multiQueryExpanderService = multiQueryExpanderService;
    }

    @PostMapping("/rag/multi-query-expander")
    public Map<String, String> answer(@RequestParam(value = "question", required = true)  String question) {
        String answer = multiQueryExpanderService.answer(question);
        return Map.of("question", question, "answer", answer);
    }
}
