package com.example.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Orchestrator-Workers 패턴 데모 애플리케이션
 * 
 * Spring AI를 사용하여 복잡한 작업을 Orchestrator가 분석하고
 * 여러 전문 Worker들이 병렬로 처리하는 패턴을 구현합니다.
 */
@SpringBootApplication
public class OrchestratorWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorWorkerApplication.class, args);
    }
}
