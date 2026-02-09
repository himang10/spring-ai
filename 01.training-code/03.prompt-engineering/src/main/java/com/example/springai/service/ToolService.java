package com.example.springai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ReAct용 Tool 기능을 제공하는 서비스
 * react-info.txt 파일의 정보를 기반으로 여러 Tool을 제공합니다.
 */
@Slf4j
@Service
public class ToolService {
    
    /**
     * react-info.txt 파일에서 ReactInfo 객체를 생성합니다.
     */
    public ReactInfo loadReactInfo() {
        try {
            ClassPathResource resource = new ClassPathResource("react-info.txt");
            String content = Files.readString(Path.of(resource.getURI()));
            log.debug("React info loaded successfully");
            return parseReactInfo(content);
        } catch (IOException e) {
            log.error("Failed to read react-info.txt", e);
            return new ReactInfo("정보 없음", "정보 없음", "정보 없음", "정보 없음");
        }
    }
    
    /**
     * 텍스트 파일 내용을 ReactInfo 객체로 파싱합니다.
     */
    private ReactInfo parseReactInfo(String content) {
        String time = extractValue(content, "현재 시간:");
        String weather = extractValue(content, "날씨:");
        String location = extractValue(content, "위치:");
        String status = extractValue(content, "현재 현황:");
        
        return new ReactInfo(time, weather, location, status);
    }
    
    private String extractValue(String content, String key) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith(key)) {
                return line.substring(key.length()).trim();
            }
        }
        return "정보 없음";
    }
    
    // ==================== Tool Functions ====================
    
    /**
     * Tool 1: 현재 시간 정보를 가져오는 Tool
     */
    @Tool(description = "현재 시간 정보를 가져옵니다. 사용자가 시간, 몇 시, 시각 등을 물어볼 때 사용하세요.")
    public String getCurrentTime() {
        log.info("Tool 'getCurrentTime' called");
        ReactInfo info = loadReactInfo();
        return "현재 시각: " + info.getTime();
    }
    
    /**
     * Tool 2: 현재 날씨 정보를 가져오는 Tool
     */
    @Tool(description = "현재 날씨 정보를 가져옵니다. 사용자가 날씨, 기온, 온도 등을 물어볼 때 사용하세요.")
    public String getWeatherInfo() {
        log.info("Tool 'getWeatherInfo' called");
        ReactInfo info = loadReactInfo();
        return "날씨 정보: " + info.getWeather();
    }
    
    /**
     * Tool 3: 현재 위치 정보를 가져오는 Tool
     */
    @Tool(description = "현재 위치 정보를 가져옵니다. 사용자가 위치, 장소, 어디 등을 물어볼 때 사용하세요.")
    public String getLocationInfo() {
        log.info("Tool 'getLocationInfo' called");
        ReactInfo info = loadReactInfo();
        return "위치 정보: " + info.getLocation();
    }
    
    /**
     * Tool 4: 현재 상황 정보를 가져오는 Tool
     */
    @Tool(description = "현재 상황 정보를 가져옵니다. 사용자가 교통, 상황, 상태 등을 물어볼 때 사용하세요.")
    public String getStatusInfo() {
        log.info("Tool 'getStatusInfo' called");
        ReactInfo info = loadReactInfo();
        return "상황 정보: " + info.getStatus();
    }
    
    /**
     * Tool 5: 모든 정보를 한번에 가져오는 Tool
     */
    @Tool(description = "모든 정보(시각, 날씨, 위치, 상황)를 한 번에 가져옵니다. 사용자가 전체 정보, 모든 정보 등을 요청하거나 복합적인 질문을 할 때 사용하세요.")
    public String getAllInfo() {
        log.info("Tool 'getAllInfo' called");
        ReactInfo info = loadReactInfo();
        return String.format("""
            [전체 정보]
            - 시각: %s
            - 날씨: %s
            - 위치: %s
            - 상황: %s""",
            info.getTime(),
            info.getWeather(),
            info.getLocation(),
            info.getStatus()
        );
    }
    
    // ==================== Data Classes ====================
    
    /**
     * ReactInfo 데이터 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactInfo {
        private String time;
        private String weather;
        private String location;
        private String status;
    }
}
