package com.prospero.budget.model;
// problem: i don't think the api has investments ...
import lombok.Data;

@Data
public class InvestmentOption {
    private String type;  // e.g., "ETF", "Savings"
    private String risk;
    private String expectedReturn;
    private String description;
}
