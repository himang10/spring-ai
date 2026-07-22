package com.example.springai.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 음악 AI 도구를 위한 REST 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class MusicController {

    @Autowired
    private MusicService musicService;

    /**
     * 음악 관련 질문을 처리합니다.
     *
     * @param message 사용자 메시지 (예: "퀸의 보헤미안 랩소디 찾아줘")
     * @return AI 응답
     */
    @PostMapping(
        value = "/music-tools",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String musicTools(@RequestParam("question") String message) {
        log.info("음악 컨트롤러 요청: {}", message);
        String answer = musicService.chat(message);
        return answer;
    }
}
