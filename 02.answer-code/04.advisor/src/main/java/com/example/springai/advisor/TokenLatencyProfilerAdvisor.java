package com.example.springai.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public class TokenLatencyProfilerAdvisor implements CallAdvisor {

    private final int warningTokenLimit;

    public TokenLatencyProfilerAdvisor(int warningTokenLimit) {
        this.warningTokenLimit = warningTokenLimit;
    }

    @Override
    public String getName() {
        return "TokenLatencyProfilerAdvisor";
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public ChatClientResponse adviseCall( ChatClientRequest request, CallAdvisorChain chain) {
        // -------------------------
        // BEFORE
        // -------------------------
        long startTime = System.nanoTime();

        try {

            // 실제 LLM 호출
            ChatClientResponse response = chain.nextCall(request);

            // -------------------------
            // AFTER
            // -------------------------
            long elapsedNanos = System.nanoTime() - startTime;
            long elapsedMillis = elapsedNanos / 1_000_000;
            logUsage(response, elapsedMillis);

            return response;

        }
        catch (RuntimeException e) {
            long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;
            log.error( "[AI PROFILE] 호출 실패 - latency={} ms", elapsedMillis);

            throw e;
        }
    }

    private void logUsage( ChatClientResponse response, long elapsedMillis) {

        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse == null) {
            log.info("[AI PROFILE] latency={} ms, usage 정보 없음", elapsedMillis);

            return;
        }

        Usage usage = chatResponse.getMetadata().getUsage();
        if (usage == null) {
            log.info( "[AI PROFILE] latency={} ms, usage 정보 없음", elapsedMillis );
            return;
        }

        Integer promptTokens = usage.getPromptTokens();
        Integer completionTokens = usage.getCompletionTokens();
        Integer totalTokens = usage.getTotalTokens();

        log.info("""
                
                ===== AI PROFILE =====
                Latency          : {} ms
                Prompt Tokens    : {}
                Completion Tokens: {}
                Total Tokens     : {}
                ======================
                """,
                elapsedMillis,
                promptTokens,
                completionTokens,
                totalTokens
        );

        if (totalTokens != null && totalTokens > warningTokenLimit) {
            log.warn( "[AI COST WARNING] Token 사용량 초과: {} > {}", totalTokens, warningTokenLimit );
        }
    }
}
