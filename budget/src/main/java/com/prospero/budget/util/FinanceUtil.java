package com.prospero.budget.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Component;

import com.prospero.budget.model.*;

@Component
public class FinanceUtil {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Map<String, MonthlySummary> getMonthlySummary(Customer customer) {
        Map<String, MonthlySummary> summaryMap = new HashMap<>();

        // --- Aggregate deposits (income) ---
        for (Deposit d : customer.getTransactions().getDeposits()) {
            String month = d.getTransactionDate().substring(0, 7); // "2025-03"
            MonthlySummary monthSummary = summaryMap.computeIfAbsent(month, k -> new MonthlySummary());
            monthSummary.setIncome(monthSummary.getIncome() + d.getAmount());
        }

        // --- Aggregate expenses ---
        for (List<?> expenseList : Arrays.asList(
                customer.getTransactions().getWithdrawals(),
                customer.getTransactions().getBills(),
                customer.getTransactions().getPurchases()
        )) {
            for (Object obj : expenseList) {
                String month = null;
                double amount = 0;
                String category = "Other";
                if (obj instanceof Withdrawal w) {
                    month = w.getTransactionDate().substring(0, 7);
                    amount = w.getAmount();
                    category = "Other"; // or w.getCategory() if you add one
                } else if (obj instanceof Bill b) {
                    month = b.getPaymentDate().substring(0, 7);
                    amount = b.getAmount();
                    category = "Bills";
                } else if (obj instanceof Purchase p) {
                    month = p.getPurchaseDate().substring(0, 7);
                    amount = p.getAmount();
                    category = p.getCategory();
                }

                MonthlySummary monthSummary = summaryMap.computeIfAbsent(month, k -> new MonthlySummary());
                Map<String, Double> expensesByCat = monthSummary.getExpensesByCategory();
                if (expensesByCat == null) expensesByCat = new HashMap<>();
                expensesByCat.put(category, expensesByCat.getOrDefault(category, 0.0) + amount);
                monthSummary.setExpensesByCategory(expensesByCat);

                // Store recent expenses for drill-down
                Map<String, List<Object>> recentMap = monthSummary.getRecentExpensesByCategory();
                if (recentMap == null) recentMap = new HashMap<>();
                recentMap.computeIfAbsent(category, k -> new ArrayList<>()).add(obj);
                monthSummary.setRecentExpensesByCategory(recentMap);
            }
        }
        return summaryMap;
    }

    public static Map<String, Map<String, Double>> getMonthlyIncomeVsExpenses(Customer customer) {
        // Map: "YYYY-MM" -> { "income" -> x, "expenses" -> y }
        Map<String, Map<String, Double>> monthlySummary = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Process deposits as income
        for (Deposit dep : customer.getTransactions().getDeposits()) {
            LocalDate date = LocalDate.parse(dep.getTransactionDate(), formatter);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

            monthlySummary.putIfAbsent(monthKey, new HashMap<>());
            Map<String, Double> monthMap = monthlySummary.get(monthKey);
            monthMap.put("income", monthMap.getOrDefault("income", 0.0) + dep.getAmount());
        }

        // Process withdrawals, bills, purchases as expenses
        List<Withdrawal> withdrawals = customer.getTransactions().getWithdrawals();
        List<Bill> bills = customer.getTransactions().getBills();
        List<Purchase> purchases = customer.getTransactions().getPurchases();

        for (Withdrawal w : withdrawals) {
            LocalDate date = LocalDate.parse(w.getTransactionDate(), formatter);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

            monthlySummary.putIfAbsent(monthKey, new HashMap<>());
            Map<String, Double> monthMap = monthlySummary.get(monthKey);
            monthMap.put("expenses", monthMap.getOrDefault("expenses", 0.0) + w.getAmount());
        }

        for (Bill b : bills) {
            LocalDate date = LocalDate.parse(b.getPaymentDate(), formatter);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

            monthlySummary.putIfAbsent(monthKey, new HashMap<>());
            Map<String, Double> monthMap = monthlySummary.get(monthKey);
            monthMap.put("expenses", monthMap.getOrDefault("expenses", 0.0) + b.getAmount());
        }

        for (Purchase p : purchases) {
            LocalDate date = LocalDate.parse(p.getPurchaseDate(), formatter);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

            monthlySummary.putIfAbsent(monthKey, new HashMap<>());
            Map<String, Double> monthMap = monthlySummary.get(monthKey);
            monthMap.put("expenses", monthMap.getOrDefault("expenses", 0.0) + p.getAmount());
        }

        return monthlySummary;
    }
}

