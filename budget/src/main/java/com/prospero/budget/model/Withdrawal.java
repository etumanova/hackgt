package com.prospero.budget.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Withdrawal {
    // withdrawal doesn't have category: just withdrawal
    private String id;
    private String accountId;
    private double amount;
    private String status;
    private String transactionDate;
    private String medium;
    private final String category = "Withdrawal";
}