package com.prospero.budget.controller;

import com.prospero.budget.service.GeminiAiIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeminiController {
    @Autowired
    private GeminiAiIntegration geminiAiIntegration;

    /*
     * Test endpoint to verify Gemini integration
     * Usage: /api/gemini/test?prompt=Examplain AI in simple terms
     */

    @GetMapping("/api/gemini/test")
    public String testGemini(@RequestParam(defaultValue = "Explain how AI works in a few words") String prompt) {
        return geminiAiIntegration.generateTextFromPrompt(prompt);
    }

    /*
     * Simple hello endpoint to test if controller is working
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello! The Spring Boot application is running.";
    }

    /*
     * Test the askGemini method with user question and budget summary
     * Usage: /test-ask-gemini?question=How%20can%20I%20save%20more%20money?
     */
    @GetMapping("/test-ask-gemini")
    public String testAskGemini(@RequestParam(defaultValue = "How can I save more money?") String question) {
        // Mock budget summary data TO EDIT WITH BUDGET SUMMARY CLASS
        String mockBudgetData = "Monthly Income: $1500, Total Expenses: $1200 (Housing: $600, Food: $300, Transportation: $150, Entertainment: $150), Net Income: $300";
        return geminiAiIntegration.generateBudgetRecommendations(mockBudgetData);
    }

    /*
     * Test budget recommendations
     */
    @GetMapping("/test-budget-ai")
    public String testBudgetRecommendations() {
        
        String sampleBudget = """
        Monthly Income: $1500, 
        Housing: $600, 
        Food: $300, 
        Transportation: $150, 
        Entertainment: $150, 
        Net Income: $300
        """;
                
        return geminiAiIntegration.generateBudgetRecommendations(sampleBudget);
    }
    /*
     * Check if API key is configured
     */
    @GetMapping("/test-config")
    public String testConfig() {
        try {
            String result = geminiAiIntegration.generateTextFromPrompt("Say 'API key is working' if you can read this.");
            return "Configuration is working! Gemini response: " + result;
        } catch (Exception e) {
            return "Configuration error: " + e.getMessage();
        }
    }
}
