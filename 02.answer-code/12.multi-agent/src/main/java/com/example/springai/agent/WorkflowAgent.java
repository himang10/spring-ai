package com.example.springai.agent;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * 여러 전문 Agent를 "정해진 순서와 조건"으로 호출하는 Workflow Agent.
 *
 * OrchestratorAgent는 어떤 Tool(전문 Agent)을 호출할지를 매번 LLM이 스스로 판단하지만(orchestrator-workers),
 * WorkflowAgent는 "1) 날씨 조회 → 2) 날씨 상태에 따라 분기해서 파일로 저장"이라는 순서와 분기 자체를
 * 코드로 고정한다(routing). 파일명 결정은 LLM 판단이 아니라 조회 결과 텍스트를 검사하는 코드가 담당한다.
 */
@Component
public class WorkflowAgent {

    private static final String RAIN_FILE_NAME = "비날씨.txt";
    private static final String CLEAR_FILE_NAME = "맑은날씨.txt";
    private static final String DEFAULT_FILE_NAME = "weather.txt";

    private static final List<String> RAIN_KEYWORDS = List.of("비", "우천", "폭우", "소나기", "강우");
    private static final List<String> CLEAR_KEYWORDS = List.of("맑음", "맑았", "맑겠", "화창");

    private final WeatherGuideAgent weatherGuideAgent;
    private final FileManagerAgent fileManagerAgent;

    public WorkflowAgent(WeatherGuideAgent weatherGuideAgent, FileManagerAgent fileManagerAgent) {
        this.weatherGuideAgent = weatherGuideAgent;
        this.fileManagerAgent = fileManagerAgent;
    }

    /**
     * 고정 순서 Workflow: 날씨 조회(Step 1) → 날씨 상태(비/맑음)에 따라 분기해 파일로 저장(Step 2).
     */
    public String runWeatherToFileWorkflow(String request, String conversationId) {
        String weatherResult = weatherGuideAgent.ask(request, conversationId);

        String fileName = resolveFileName(weatherResult);

        String fileInstruction = """
                아래 날씨 조회 결과를 "%s" 파일명으로 저장해줘.

                %s
                """.formatted(fileName, weatherResult);

        String fileResult = fileManagerAgent.ask(fileInstruction, conversationId);

        return """
                [1단계: 날씨 조회 결과]
                %s

                [2단계: 파일 저장 결과 (%s)]
                %s
                """.formatted(weatherResult, fileName, fileResult);
    }

    /**
     * 날씨 조회 결과에 비/맑음 관련 키워드가 있는지로 저장할 파일명을 결정한다(코드 기반 routing).
     */
    private String resolveFileName(String weatherResult) {
        if (RAIN_KEYWORDS.stream().anyMatch(weatherResult::contains)) {
            return RAIN_FILE_NAME;
        }
        if (CLEAR_KEYWORDS.stream().anyMatch(weatherResult::contains)) {
            return CLEAR_FILE_NAME;
        }
        return DEFAULT_FILE_NAME;
    }
}
