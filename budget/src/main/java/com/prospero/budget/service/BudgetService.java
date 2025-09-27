package com.prospero.budget.service;

import com.prospero.budget.model.BudgetSummary;
import com.prospero.budget.model.Transaction;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BudgetService {

    public BudgetSummary calculateSummary(List<Transaction> transactions) {
        double income = 0, expenses = 0;
        Map<String, Double> categories = new HashMap<>();

        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("deposit")) income += t.getAmount();
            else expenses += t.getAmount();

            categories.merge(t.getCategory(), t.getAmount(), Double::sum);
        }

        BudgetSummary summary = new BudgetSummary();
        summary.setTotalIncome(income);
        summary.setTotalExpenses(expenses);
        summary.setCategories(categories);
        summary.setNetIncome(income - expenses);
        return summary;
    }
}

