package com.prospero.budget.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bill {
    // payee: id of customer i believe
    // account ID: account that this is going to be paid from
    private String id;
    private String accountId;
    private double amount;
    private String status;
    private String payee; // payee: who the payment is made for i believe
    private String paymentDate; // YYYY-MM-DD (payment_date)
    private final String category = "bill";
}
