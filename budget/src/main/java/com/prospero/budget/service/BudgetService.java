package com.prospero.budget.service;

import com.prospero.budget.model.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BudgetService {

    public BudgetSummary calculateSummary(CustomerTransactions transactions) {
        double income = 0, expenses = 0;
        Map<String, Double> categories = new HashMap<>();

        for (Purchase p : transactions.getPurchases()) {
            expenses += p.getAmount();
            categories.merge(p.getCategory(), p.getAmount(), Double::sum);
        }

        for (Deposit d : transactions.getDeposits()) {
            income += d.getAmount();
        }

        for (Bill b : transactions.getBills()) {
            expenses += b.getAmount();
            categories.merge(b.getCategory(), b.getAmount(), Double::sum);
        }

        for (Withdrawal w : transactions.getWithdrawals()) {
            expenses += w.getAmount();
            categories.merge(w.getCategory(), w.getAmount(), Double::sum);
        }

        BudgetSummary summary = new BudgetSummary();
        summary.setTotalIncome(income);
        summary.setTotalExpenses(expenses);
        summary.setCategories(categories);
        summary.setNetIncome(income - expenses);
        return summary;
    }
}

