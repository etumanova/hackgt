package com.prospero.budget.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class Customer {
    private String customerId;
    private String name;
    private List<Account> accounts;
    // Customer Transactions
    private CustomerTransactions transactions; // contains deposits, purchases, bills, withdrawals
}
