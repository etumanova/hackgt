// package com.prospero.budget.controller;

// import com.prospero.budget.model.BudgetSummary;
// import com.prospero.budget.model.InvestmentOption;
// import com.prospero.budget.service.BudgetService;
// import com.prospero.budget.service.InvestmentService;
// import com.prospero.budget.service.NessieService;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/investments")
// @CrossOrigin(origins = "*")
// public class InvestmentController {
//     private final NessieService nessieService;
//     private final BudgetService budgetService;
//     private final InvestmentService investmentService;

//     public InvestmentController(NessieService nessieService, BudgetService budgetService, InvestmentService investmentService) {
//         this.nessieService = nessieService;
//         this.budgetService = budgetService;
//         this.investmentService = investmentService;
//     }

//     @GetMapping
//     public List<InvestmentOption> getRecommendations(@RequestParam(defaultValue = "demoUser") String customerId) {
//         //BudgetSummary summary = budgetService.calculateSummary(nessieService.getTransactions(customerId));
//         //return investmentService.recommendInvestments(summary);
//     }
// }
