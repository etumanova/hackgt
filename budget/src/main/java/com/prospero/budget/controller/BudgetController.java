package com.prospero.budget.controller;

import com.prospero.budget.model.Customer;
import com.prospero.budget.model.MonthlySummary;
import com.prospero.budget.service.NessieService;
import com.prospero.budget.util.*;

import java.lang.ProcessBuilder.Redirect;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class BudgetController {

    @Autowired
    private NessieService nessieService;

    @Autowired
    private FinanceUtil finance;

    @GetMapping("/budget")
    public ResponseEntity<Map<String, MonthlySummary>> getBudget() {
        Customer customer = nessieService.initializeSampleCustomer(); // fetch the full customer object
        Map<String, MonthlySummary> summary = finance.getMonthlySummary(customer);
        return ResponseEntity.ok(summary);
    }
}
