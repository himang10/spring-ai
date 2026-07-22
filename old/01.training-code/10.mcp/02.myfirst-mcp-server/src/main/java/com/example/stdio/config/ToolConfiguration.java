package com.example.stdio.config;

import com.example.stdio.tool.ProductTools;
import com.example.stdio.tool.UserTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server Tool Configuration
 * 도구를 ToolCallbackProvider로 등록하는 설정 클래스
 */
@Configuration
public class ToolConfiguration {
    @Autowired
    private UserTools userTools;

    @Autowired
    private ProductTools productTools;

    @Bean
    public ToolCallbackProvider getToolCallbackProvider() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(
                        userTools,
                        productTools
                )
                .build();
    }
}
