package com.prospero.budget.model;

import lombok.Data;

@Data
public class Transaction {
    private String id;
    private String type;      // e.g. "deposit" or "withdrawal"
    private double amount;
    private String category;  // "food", "rent", "entertainment"
    private String date;
}