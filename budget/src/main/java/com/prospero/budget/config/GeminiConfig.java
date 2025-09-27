package com.prospero.budget.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties(prefix = "api")
public class GeminiConfig {
    @Value("${api.gemini-key}")
    private String geminiApiKey;

    public String getGeminiApiKey() {
        return geminiApiKey;
    }
}