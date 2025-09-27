package com.prospero.budget.model;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class BudgetSummary {
    private double totalIncome;
    private double totalExpenses;
    private Map<String, Double> categories = new HashMap<>();
    private double netIncome;

    public void addExpense(String category, double amount) {
        categories.put(category, categories.getOrDefault(category, 0.0) + amount);
        totalExpenses += amount;
        calculateNetIncome();
    }

    public void setIncome(double income) {
        this.totalIncome = income;
        calculateNetIncome();
    }

    public void calculateNetIncome() {
        this.netIncome = totalIncome - totalExpenses;
    }
}
