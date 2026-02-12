package com.example.springai.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 조회 AI 도구를 위한 REST 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    /**
     * 날씨 관련 질문을 처리합니다.
     *
     * @param question 사용자 질문 (예: "서울 날씨 어때?")
     * @return AI 응답
     */
    @PostMapping(
        value = "/weather-tools",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String weatherTools(@RequestParam("question") String question) {
        log.info("날씨 컨트롤러 요청: {}", question);
        String answer = weatherService.chat(question);
        return answer;
    }
}
