package com.prospero.budget.model;

import lombok.Data;
import java.util.Map;

@Data
public class BudgetSummary {
    private double totalIncome;
    private double totalExpenses;
    private Map<String, Double> categories;
    private double netIncome;
}
