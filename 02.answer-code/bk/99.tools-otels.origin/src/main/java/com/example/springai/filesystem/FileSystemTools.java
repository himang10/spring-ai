package com.example.springai.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 시스템 작업을 위한 AI 도구를 제공합니다.
 */
@Component
@Slf4j
public class FileSystemTools {

    private static final String FS_TOOLS_DIR = "fs-tools";
    private final Path baseDirectory;

    public FileSystemTools() {
        // 현재 Spring Boot 실행 위치 기준으로 fs-tools 디렉토리 설정
        this.baseDirectory = Paths.get(System.getProperty("user.dir"), FS_TOOLS_DIR);
        ensureDirectoryExists();
    }

    /**
     * fs-tools 디렉토리가 존재하는지 확인하고 없으면 생성합니다.
     */
    private void ensureDirectoryExists() {
        try {
            if (!Files.exists(baseDirectory)) {
                Files.createDirectories(baseDirectory);
                log.info("fs-tools 디렉토리 생성: {}", baseDirectory.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("fs-tools 디렉토리 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 사용자 질문을 txt 파일로 저장합니다.
     * 파일명은 타임스탬프를 포함하여 자동 생성됩니다.
     *
     * @param question 저장할 질문 내용
     * @return 저장된 파일 정보
     */
    @Tool(description = "사용자의 질문을 텍스트 파일로 저장합니다. 파일은 fs-tools 디렉토리에 타임스탬프와 함께 저장됩니다.")
    public String saveQuestionToFile(
            @ToolParam(description = "파일에 저장할 질문 내용", required = true) 
            String question) {
        
        ensureDirectoryExists();
        
        try {
            // 타임스탬프를 포함한 파일명 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "question_" + timestamp + ".txt";
            Path filePath = baseDirectory.resolve(fileName);
            
            // 파일에 질문 저장
            Files.writeString(filePath, question);
            
            StringBuilder result = new StringBuilder();
            result.append("질문이 파일에 저장되었습니다.\n");
            result.append("- 파일명: ").append(fileName).append("\n");
            result.append("- 경로: ").append(filePath.toAbsolutePath()).append("\n");
            result.append("- 내용: ").append(question);
            
            log.info("질문 저장 완료: {}", filePath.toAbsolutePath());
            return result.toString();
            
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage(), e);
            return "파일 저장 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * fs-tools 디렉토리에 있는 모든 파일 목록을 조회합니다.
     *
     * @return 파일 목록 정보
     */
    @Tool(description = "fs-tools 디렉토리에 저장된 파일 목록을 조회합니다.")
    public String listFiles() {
        
        ensureDirectoryExists();
        
        try {
            File dir = baseDirectory.toFile();
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
            
            if (files == null || files.length == 0) {
                return "저장된 파일이 없습니다.";
            }
            
            StringBuilder result = new StringBuilder();
            result.append(String.format("총 %d개 파일:\n", files.length));
            
            for (int i = 0; i < files.length; i++) {
                result.append(String.format("%d. %s\n", i + 1, files[i].getName()));
            }
            
            log.info("파일 목록 조회 완료: {}개", files.length);
            return result.toString();
            
        } catch (Exception e) {
            log.error("파일 목록 조회 실패: {}", e.getMessage(), e);
            return "파일 목록 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 파일을 삭제합니다.
     * - 특정 파일명을 지정하면 해당 파일 삭제
     * - "최근" 또는 "latest"를 지정하면 가장 최근 파일 삭제
     * - "모두" 또는 "all"을 지정하면 모든 파일 삭제
     *
     * @param target 삭제할 대상 (파일명, "최근", "모두")
     * @return 삭제 결과 메시지
     */
    @Tool(description = """
            파일을 삭제합니다. 
            - 파일명을 입력하면 해당 파일 삭제
            - '최근' 또는 'latest'를 입력하면 가장 최근에 생성된 파일 삭제
            - '모두' 또는 'all'을 입력하면 모든 파일 삭제
            """)
    public String deleteFile(
            @ToolParam(description = "삭제할 파일명 또는 '최근', '모두' 키워드", required = true) 
            String target) {
        
        ensureDirectoryExists();
        
        try {
            File dir = baseDirectory.toFile();
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
            
            if (files == null || files.length == 0) {
                return "삭제할 파일이 없습니다.";
            }
            
            // "모두" 또는 "all" 키워드 체크
            if (target.equalsIgnoreCase("모두") || target.equalsIgnoreCase("all")) {
                int deletedCount = 0;
                for (File file : files) {
                    if (file.delete()) {
                        deletedCount++;
                        log.info("파일 삭제: {}", file.getName());
                    }
                }
                return String.format("총 %d개 파일을 삭제했습니다.", deletedCount);
            }
            
            // "최근" 또는 "latest" 키워드 체크
            if (target.equalsIgnoreCase("최근") || target.equalsIgnoreCase("latest")) {
                // 가장 최근 파일 찾기 (lastModified 기준)
                File latestFile = files[0];
                for (File file : files) {
                    if (file.lastModified() > latestFile.lastModified()) {
                        latestFile = file;
                    }
                }
                
                if (latestFile.delete()) {
                    log.info("최근 파일 삭제: {}", latestFile.getName());
                    return String.format("최근 파일을 삭제했습니다: %s", latestFile.getName());
                } else {
                    return "파일 삭제에 실패했습니다.";
                }
            }
            
            // 특정 파일명으로 삭제
            for (File file : files) {
                if (file.getName().equals(target) || file.getName().contains(target)) {
                    if (file.delete()) {
                        log.info("파일 삭제: {}", file.getName());
                        return String.format("파일을 삭제했습니다: %s", file.getName());
                    } else {
                        return "파일 삭제에 실패했습니다.";
                    }
                }
            }
            
            return String.format("'%s' 파일을 찾을 수 없습니다.", target);
            
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            return "파일 삭제 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
