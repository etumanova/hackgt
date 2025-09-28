package com.prospero.budget.model;


import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlySummary {
    private double income; // total deposits
    private Map<String, Double> expensesByCategory; // Food, Rent, Entertainment, etc.
    private Map<String, List<Object>> recentExpensesByCategory; // recent Withdrawals, Bills, Purchases
}

