package com.example.springai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/settings")
public class SettingsController {
    
    private static final String UPLOAD_DIR = "uploads/";
    
    /**
     * 파일 업로드 처리
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        log.info("File upload request received: {}", file.getOriginalFilename());
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 파일 저장
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("File uploaded successfully: {}", fileName);
            
            response.put("status", "success");
            response.put("message", "파일이 성공적으로 업로드되었습니다.");
            response.put("fileName", fileName);
            response.put("filePath", filePath.toString());
            
            // 다음 단계에서 벡터 DB에 저장하는 로직 추가 예정
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading file", e);
            response.put("status", "error");
            response.put("message", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
