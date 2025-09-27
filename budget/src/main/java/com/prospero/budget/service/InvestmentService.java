package com.prospero.budget.service;

import com.prospero.budget.model.BudgetSummary;
import com.prospero.budget.model.InvestmentOption;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvestmentService {

    public List<InvestmentOption> recommendInvestments(BudgetSummary summary) {
        double surplus = summary.getNetIncome();
        List<InvestmentOption> options = new ArrayList<>();

        if (surplus < 100) {
            options.add(new InvestmentOption() {{
                setType("Savings");
                setRisk("Low");
                setExpectedReturn("2%");
                setDescription("Consider building your emergency fund first.");
            }});
        } else if (surplus < 500) {
            options.add(new InvestmentOption() {{
                setType("ETF");
                setRisk("Medium");
                setExpectedReturn("6%");
                setDescription("Diversified, steady growth.");
            }});
        } else {
            options.add(new InvestmentOption() {{
                setType("Stocks");
                setRisk("High");
                setExpectedReturn("10%");
                setDescription("Higher return potential for your surplus.");
            }});
        }

        return options;
    }
}

