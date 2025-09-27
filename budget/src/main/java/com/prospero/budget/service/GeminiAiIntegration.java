package com.prospero.budget.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAiIntegration {

    @Value("${api.gemini-key}")
    private String apiKey;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public GeminiAiIntegration() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Simple method to test Gemini API integration
     */
    public String generateTextFromPrompt(String prompt) {
        try {
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            return "Unable to generate response at this time.";
        }
    }
    
    /**
     * Generate budget recommendations using Gemini AI
     */
    public String generateBudgetRecommendations(String budgetData) {
        String prompt = "As a financial advisor for college students, analyze this budget data and provide 3-4 specific, actionable recommendations:\n\n" + budgetData;
        return generateTextFromPrompt(prompt);
    }
    
    /**
     * Core method to call Gemini API
     */
    private String callGeminiAPI(String prompt) throws Exception {
        // Check if API key is available
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("your-gemini-api-key")) {
            throw new RuntimeException("Gemini API key is not configured. Please set GEMINI_API_KEY environment variable.");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create the request body according to Gemini API specification
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        
        part.put("text", prompt);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));
        
        // Add generation config for better responses
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.put("generationConfig", generationConfig);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // Make the API call with API key in URL
        String urlWithKey = GEMINI_API_URL + "?key=" + apiKey;
        ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, request, String.class);
        
        // Parse the response JSON
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode candidatesNode = responseJson.path("candidates");
        
        if (candidatesNode.isArray() && candidatesNode.size() > 0) {
            JsonNode firstCandidate = candidatesNode.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            JsonNode partsNode = contentNode.path("parts");
            
            if (partsNode.isArray() && partsNode.size() > 0) {
                return partsNode.get(0).path("text").asText();
            }
        }
        
        throw new RuntimeException("Unable to parse Gemini API response: " + response.getBody());
    }
}
