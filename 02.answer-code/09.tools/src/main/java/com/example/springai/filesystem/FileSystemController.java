package com.example.springai.filesystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 시스템 AI 도구를 위한 REST 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class FileSystemController {

    @Autowired
    private FileSystemService fileSystemService;

    /**
     * 파일 시스템 관련 질문을 처리합니다.
     *
     * @param question 사용자 질문 (예: "이 질문을 파일로 저장해줘", "어떤 파일이 있어?")
     * @return AI 응답
     */
    @PostMapping(
        value = "/filesystem-tools",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String fileSystemTools(@RequestParam("question") String question) {
        log.info("파일시스템 컨트롤러 요청: {}", question);
        String answer = fileSystemService.chat(question);
        return answer;
    }
}
