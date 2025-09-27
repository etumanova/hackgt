package com.prospero.budget.service;

import com.prospero.budget.config.ApiConfig;
import com.prospero.budget.model.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Service
public class NessieService {

    private final ApiConfig apiConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public NessieService(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public List<Transaction> getTransactions(String customerId) {
        String url = String.format("%s/customers/%s/accounts?key=%s",
                apiConfig.getBaseUrl(), customerId, apiConfig.getApiKey());

        // For demo: mock data if API call fails
        try {
            Transaction[] transactions = restTemplate.getForObject(url, Transaction[].class);
            return Arrays.asList(transactions);
        } catch (Exception e) {
            return List.of(
                new Transaction() {{ setType("deposit"); setAmount(2000); setCategory("income"); }},
                new Transaction() {{ setType("withdrawal"); setAmount(800); setCategory("rent"); }},
                new Transaction() {{ setType("withdrawal"); setAmount(400); setCategory("food"); }}
            );
        }
    }
}
