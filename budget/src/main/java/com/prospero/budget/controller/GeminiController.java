package com.prospero.budget.controller;

import com.prospero.budget.service.GeminiAiIntegration;
import com.prospero.budget.model.BudgetSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GeminiController {
    @Autowired
    private GeminiAiIntegration geminiAiIntegration;

    @PostMapping("/api/gemini/chat")
    public Map<String, String>handleChatMessage(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();

        try {
            String userMessage = request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                response.put("reply", "I didn't receive your message. Please try again");
                return response;
            }

            // Determine question type: "budgeting" default or "investment"
            String type = (String) request.getOrDefault("type", "budgeting");

            String prompt;

            if ("investment".equalsIgnoreCase(type)) {
                // Extracting investment params or using defaults
                Double availableAmount = request.get("amount") != null ?
                    Double.valueOf(request.get("amount").toString()) : 0.0;
                String timeHorizon = (String) request.getOrDefault("timeHorizon", "4 years");
                String riskTolerance = (String) request.getOrDefault("riskTolerance", "moderate");

                prompt = String.format(
                    "You are a investment advisor for college students. The user has $%.2f to invest with a time horizon of %s and a %s risk tolerance. " +
                    "They asked: '%s'. Provide practical, beginner-friendly investment advice in 3-4 sentences. " +
                    "Focus on low-cost index funds, ETFs, and student-appropriate strategies. " +
                    "Always emphasize the importance of emergency funds first. " +
                    "Mention specific platforms like Robinhood, Fidelity, or Vanguard when relevant. ",
                    userMessage.trim(), availableAmount, timeHorizon, riskTolerance);
                } else {
                    // Default to budgeting advice prompt
                    prompt = String.format(
                        "You are a helpful financial assistant for college students using a budgeting app called 'Prospera'." +
                        "The user asked: '%s'. " +
                        "Provide practical, friendly advice in 2-3 sentences. " +
                        "Focus on student-specific financial tips when relevant.",
                        userMessage.trim()
                    );
                }

                String geminiResponse = geminiAiIntegration.generateTextFromPrompt(prompt);
                response.put("reply", geminiResponse);
        } catch (Exception e) {
            System.err.println("Chat error: " + e.getMessage());
            response.put("reply", "Sorry, I'm having trouble responding right now.");
        }
        return response;
    }

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
        // Creating a realistic BudgetSummary for a college student
        BudgetSummary mockBudget = new BudgetSummary();

        // Set income
        mockBudget.setIncome(2000.0);

        // Add typical college expenses
        mockBudget.addExpense("Rent", 900.0);
        mockBudget.addExpense("Food", 400.0);
        mockBudget.addExpense("Tuition", 600.0);
        mockBudget.addExpense("Bills", 200.0);
        mockBudget.addExpense("Others", 300.0);
        
        return geminiAiIntegration.askGemini(question, mockBudget);
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
