package com.prospero.budget.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfig {
    @Value("${api.nessie-key}")
    private String nessieApiKey;

    private final String baseUrl = "api.nessie-base-url";

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return nessieApiKey;
    }
}
