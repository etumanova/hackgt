package com.prospero.budget.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CustomerTransactions {
    private List<Deposit> deposits;
    private List<Withdrawal> withdrawals;
    private List<Bill> bills;
    private List<Purchase> purchases;
}
