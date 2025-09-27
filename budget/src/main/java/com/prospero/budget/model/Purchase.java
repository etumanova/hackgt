package com.prospero.budget.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Purchase {
    private String id;
    private double amount;
    //private String merchantId;
    private String purchaseDate; // format: YYYY-MM-DD
    private String merchantName; // the name of the merchant that the purchase was made from
    private String category;  // "food", "rent", "entertainment"
}
