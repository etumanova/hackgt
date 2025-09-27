package com.prospero.budget.controller;

import com.prospero.budget.model.ChatRequest;
import com.prospero.budget.model.ChatResponse;
import com.prospero.budget.service.BudgetService;
import com.prospero.budget.service.GeminiService;
import com.prospero.budget.service.NessieService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final GeminiService geminiService;
    private final BudgetService budgetService;
    private final NessieService nessieService;

    public ChatController(GeminiService geminiService, BudgetService budgetService, NessieService nessieService) {
        this.geminiService = geminiService;
        this.budgetService = budgetService;
        this.nessieService = nessieService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        var transactions = nessieService.getTransactions("demoUser");
        var summary = budgetService.calculateSummary(transactions);
        String answer = geminiService.askGemini(request.getQuestion(), summary);

        ChatResponse response = new ChatResponse();
        response.setAnswer(answer);
        return response;
    }
}

