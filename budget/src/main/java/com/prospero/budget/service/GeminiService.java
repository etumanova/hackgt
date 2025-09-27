package com.prospero.budget.service;

import com.prospero.budget.config.GeminiConfig;
import com.prospero.budget.model.BudgetSummary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class GeminiService {
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiService(GeminiConfig geminiConfig) {
        this.geminiConfig = geminiConfig;
    }

    public String askGemini(String userQuestion, BudgetSummary summary) {
        String prompt = String.format(
            "You are a personal finance assistant. The user asked: '%s'. " +
            "Given this budget summary: %s, respond briefly and clearly.",
            userQuestion, summary.toString()
        );

        // Pseudo-request; replace with actual Gemini endpoint & payload
        Map<String, String> body = Map.of("prompt", prompt);
        // In hackathon, mock this if needed:
        return "You earned $" + summary.getTotalIncome() + " and spent $" + summary.getTotalExpenses() +
               ", leaving you with $" + summary.getNetIncome() + ".";
    }
}

