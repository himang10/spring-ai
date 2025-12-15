package com.example.springai.controller;

import com.example.springai.service.AdvisorService;
import com.example.springai.service.BasicService;
import com.example.springai.service.MaxCharLengthService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Advisor 기능을 테스트하는 REST 컨트롤러.
 * SimpleLoggingAdvisor와 JsonLoggingAdvisor를 각각 테스트할 수 있는 엔드포인트를 제공합니다.
 * 
 * @author SKALA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/chat")
public class AdvisorController {

    private final BasicService basicService;
    private final AdvisorService advisorService;
    private final MaxCharLengthService maxCharLengthService;

    public AdvisorController(BasicService basicService, AdvisorService advisorService, 
                           MaxCharLengthService maxCharLengthService) {
        this.basicService = basicService;
        this.advisorService = advisorService;
        this.maxCharLengthService = maxCharLengthService;
    }

    /**
     * BasicService를 사용하여 LLM을 호출합니다.
     * @param request
     * @return
     */
    @PostMapping("/basic")
    public Map<String, String> chatWithBasicAdvisor(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = basicService.callAdvisor(message);
        return Map.of("message", response);
    }

    /**
     * SimpleLoggingAdvisor를 사용하여 LLM을 호출합니다.
     * 콘솔에 간단한 텍스트 형식으로 요청/응답이 로깅됩니다.
     * 
     * @param request 사용자 메시지를 포함한 요청 맵
     * @return AI 응답을 포함한 맵
     */
    @PostMapping("/simple")
    public Map<String, String> chatWithSimpleLogging(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = advisorService.callWithSimpleLogging(message);
        return Map.of("message", response);
    }

    /**
     * JsonLoggingAdvisor를 사용하여 LLM을 호출합니다.
     * 콘솔에 JSON 형식으로 요청/응답이 로깅됩니다.
     * 
     * @param request 사용자 메시지를 포함한 요청 맵
     * @return AI 응답을 포함한 맵
     */
    @PostMapping("/json")
    public Map<String, String> chatWithJsonLogging(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = advisorService.callWithJsonLogging(message);
        return Map.of("message", response);
    }

    /**
     * MaxCharLengthAdvisor를 사용하여 LLM을 호출합니다.
     * 응답 길이를 100자로 제한합니다.
     * 
     * @param request 사용자 메시지를 포함한 요청 맵
     * @return AI 응답을 포함한 맵
     */
    @PostMapping("/maxchar")
    public Map<String, String> chatWithMaxCharLength(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = maxCharLengthService.advisorContext(message);
        return Map.of("message", response);
    }
}
