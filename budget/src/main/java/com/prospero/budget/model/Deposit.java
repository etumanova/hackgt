package com.prospero.budget.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Deposit {
    private String id;
    private double amount;
    private String transactionDate;
    private String status;
    private String type;
    private final String category = "deposit";
}
