package com.prospero.budget.controller;

import com.prospero.budget.model.Customer;
import com.prospero.budget.service.NessieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    @Autowired
    private NessieService nessieService;

    // initializing a sample customer
    @GetMapping("/init")
    public ResponseEntity<Customer> initCustomer() {
        Customer profile = nessieService.initializeSampleCustomer();
        return ResponseEntity.ok(profile);
    }

}

//     private final NessieService nessieService;
//     private final BudgetService budgetService;
//     private final BudgetSummary budgetSummary = new BudgetSummary();

//     public BudgetController(NessieService nessieService, BudgetService budgetService) {
//         this.nessieService = nessieService;
//         this.budgetService = budgetService;
//     }

//     @PostMapping("/income")
//     public String setIncome(@RequestParam double income) {
//         budgetSummary.setIncome(income);
//         return "Income set to " + income;
//     }

//     @PostMapping("/expense")
//     public String addExpense(@RequestParam String category, @RequestParam double amount) {
//         budgetSummary.addExpense(category, amount);
//         return "Added " + amount + " to " + category;
//     }

//     @GetMapping("/summary")
//     public BudgetSummary getBudgetSummary(@RequestParam(defaultValue = "demoUser") String customerId) {
//         var transactions = nessieService.getTransactions(customerId);
//         return budgetService.calculateSummary(transactions);
//     }
// }
