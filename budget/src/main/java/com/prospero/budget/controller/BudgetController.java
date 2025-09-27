package com.prospero.budget.controller;

import com.prospero.budget.model.BudgetSummary;
import com.prospero.budget.service.BudgetService;
import com.prospero.budget.service.NessieService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*") // for frontend
public class BudgetController {

    private final NessieService nessieService;
    private final BudgetService budgetService;

    public BudgetController(NessieService nessieService, BudgetService budgetService) {
        this.nessieService = nessieService;
        this.budgetService = budgetService;
    }

    @GetMapping("/summary")
    public BudgetSummary getBudgetSummary(@RequestParam(defaultValue = "demoUser") String customerId) {
        var transactions = nessieService.getTransactions(customerId);
        return budgetService.calculateSummary(transactions);
    }
}
