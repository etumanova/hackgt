package com.prospero.budget.service;

import com.prospero.budget.config.ApiConfig;
import com.prospero.budget.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NessieService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final ApiConfig apiConfig;
    private final String apiKey;
    private Map<String, List<String>> categoryMerchants;
    private List<String> allMerchants;
    private CustomerTransactions transactions;

    @Autowired
    public NessieService(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.apiKey = "?key=" + apiConfig.getNessieApiKey();
        this.transactions = new CustomerTransactions();
        this.categoryMerchants = Map.of(
            "Food", Arrays.asList("Chipotle", "Starbucks", "Whole Foods", "McDonald's", "Subway"),
            "Entertainment", Arrays.asList("Netflix", "Spotify", "Hulu", "Apple Store", "Steam"),
            "Rent", Arrays.asList("Landlord"),
            "Transport", Arrays.asList("Uber", "Lyft", "Shell", "Delta Airlines"),
            "Tuition", Arrays.asList("Georgia Tech", "Coursera", "edX"),
            "Other", Arrays.asList("Amazon", "Target", "Airbnb", "Best Buy"));
        // flatten all merchants into a list
        this.allMerchants = categoryMerchants.values()
                        .stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
    }

    public Customer initializeSampleCustomer() {
        // this imports all of the enterprise customers, chooses a random customer, and then finds the 
        // set of all the accounts and transactions (deposits, bills, withdrawals)
        return importAndCloneEnterpriseCustomer();
    }

    private Customer importAndCloneEnterpriseCustomer() {
        Random rand = new Random();

        // === 1. Fetch enterprise customers ===
        String enterpriseCustomerUrl = apiConfig.getNessieBaseUrl() + "/enterprise/customers" + apiKey;
        ResponseEntity<Map> customerResponse = restTemplate.exchange(
                enterpriseCustomerUrl, HttpMethod.GET, null, Map.class
        );
        List<Map<String, Object>> customers = (List<Map<String, Object>>) customerResponse.getBody().get("results");

        // Pick a random enterprise customer
        Map<String, Object> selectedCustomer = customers.get(rand.nextInt(customers.size()));
        System.out.println("Selected enterprise customer: " + selectedCustomer.get("_id"));

        // === 2. Create new customer in sandbox ===
        Map<String, Object> newCustomerData = Map.of(
                "first_name", selectedCustomer.get("first_name"),
                "last_name", selectedCustomer.get("last_name"),
                "address", selectedCustomer.get("address")
        );
        ResponseEntity<Map> newCustomerResponse = restTemplate.postForEntity(
                apiConfig.getNessieBaseUrl() + "/customers" + apiKey,
                newCustomerData,
                Map.class
        );

        String newCustomerId = (String) ((Map<String, Object>) newCustomerResponse.getBody().get("objectCreated")).get("_id");
        String customerName = selectedCustomer.get("first_name") + " " + selectedCustomer.get("last_name");
        System.out.println("Created new customer: " + newCustomerId);

        // === 3. Generate random accounts ===
        int numAccounts = rand.nextInt(3) + 3; // 3–5 accounts
        List<Account> accounts = new ArrayList<>();

        for (int i = 0; i < numAccounts; i++) {
            Map<String, Object> accountData = Map.of(
                    "type", rand.nextBoolean() ? "Checking" : "Savings",
                    "nickname", "Account " + (i + 1),
                    "rewards", rand.nextInt(2000),
                    "balance", (double)((int)((rand.nextDouble() * 5000)*100))/100
            );

            ResponseEntity<Map> accResp = restTemplate.postForEntity(
                    apiConfig.getNessieBaseUrl() + "/customers/" + newCustomerId + "/accounts" + apiKey,
                    accountData,
                    Map.class
            );

            String accountId = (String) ((Map<String, Object>) accResp.getBody().get("objectCreated")).get("_id");
            Account acc = new Account();
            acc.setId(accountId);
            acc.setType((String) accountData.get("type"));
            acc.setNickname((String) accountData.get("nickname"));
            acc.setBalance(((Number) accountData.get("balance")).doubleValue());
            accounts.add(acc);
        }

        // === 4. Generate random transactions for each account ===
        List<Deposit> deposits = new ArrayList<>();
        List<Withdrawal> withdrawals = new ArrayList<>();
        List<Bill> bills = new ArrayList<>();
        List<Purchase> purchases = new ArrayList<>();

        // make a number of deposits, withdrawals, and bills per account
        for (Account acc : accounts) {
            // Random deposits
            int numDeposits = rand.nextInt(30) + 30; // 30 to 60 deposits
            for (int i = 0; i < numDeposits; i++) {
                double amount = (double)((int)((rand.nextDouble() * 1000 + 50)*100))/100;
                int dayNum = rand.nextInt(27) + 1; // 1–27
                String day = (dayNum < 10 ? "0" : "") + dayNum;
                Map<String, Object> depositData = Map.of(
                        "medium", "balance",
                        "transaction_date", "2025-0" + (rand.nextInt(9) + 1) + "-" + day,
                        "status", "completed",
                        "amount", amount
                );
                ResponseEntity<Map> depResp = restTemplate.postForEntity(
                        apiConfig.getNessieBaseUrl() + "/accounts/" + acc.getId() + "/deposits" + apiKey,
                        depositData,
                        Map.class
                );

                Deposit dep = new Deposit();
                dep.setId((String) ((Map<String, Object>) depResp.getBody().get("objectCreated")).get("_id"));
                dep.setAmount(amount);
                dep.setTransactionDate((String) depositData.get("transaction_date"));
                dep.setStatus("completed");
                dep.setType("deposit");
                deposits.add(dep);
            }

            // Random withdrawals
            int numWithdrawals = rand.nextInt(10) + 10;
            for (int i = 0; i < numWithdrawals; i++) {
                double amount = (double)((int)((rand.nextDouble() * 500 + 20)*100))/100;
                int dayNum = rand.nextInt(27) + 1; // 1–27
                String day = (dayNum < 10 ? "0" : "") + dayNum;
                Map<String, Object> withdrawalData = Map.of(
                        "medium", "balance",
                        "transaction_date", "2025-0" + (rand.nextInt(9) + 1) + "-" + day,
                        "status", "completed",
                        "amount", amount
                );
                ResponseEntity<Map> wResp = restTemplate.postForEntity(
                        apiConfig.getNessieBaseUrl() + "/accounts/" + acc.getId() + "/withdrawals" + apiKey,
                        withdrawalData,
                        Map.class
                );

                Withdrawal w = new Withdrawal();
                w.setId((String) ((Map<String, Object>) wResp.getBody().get("objectCreated")).get("_id"));
                w.setAccountId(acc.getId());
                w.setAmount(amount);
                w.setStatus("completed");
                w.setMedium("balance");
                w.setTransactionDate((String) withdrawalData.get("transaction_date"));
                withdrawals.add(w);
            }

            // Random bills
            int numBills = rand.nextInt(10);
            String merchant = allMerchants.get(rand.nextInt(allMerchants.size()));

            // Find its category
            String category = categoryMerchants.entrySet()
                    .stream()
                    .filter(e -> e.getValue().contains(merchant))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("Other");

            for (int i = 0; i < numBills; i++) {
                double amount = (double)((int)((rand.nextDouble() * 300 + 30)*100))/100;
                int dayNum = rand.nextInt(27) + 1; // 1–27
                String day = (dayNum < 10 ? "0" : "") + dayNum;
                Map<String, Object> billData = Map.of(
                        "status", "pending",
                        "payee", merchant,
                        "nickname", "Bill Payment",
                        "payment_date", "2025-0" + (rand.nextInt(9) + 1) + "-" + day,
                        "payment_amount", amount
                );
                ResponseEntity<Map> billResp = restTemplate.postForEntity(
                        apiConfig.getNessieBaseUrl() + "/accounts/" + acc.getId() + "/bills" + apiKey,
                        billData,
                        Map.class
                );

                Bill bill = new Bill();
                Map<String, Object> createdBill = (Map<String, Object>) billResp.getBody().get("objectCreated");
                bill.setId((String) (createdBill).get("_id"));
                bill.setAccountId(acc.getId());
                bill.setAmount(amount);
                bill.setPaymentDate((String) billData.get("payment_date"));
                bill.setPayee((String) billData.get("payee"));
                bill.setStatus((String) createdBill.get("status"));
                bills.add(bill);
            }

            // === Purchases ===
            int numPurchases = rand.nextInt(15) + 40; // 40–55 purchases per account

            // Random purchases
            for (int i = 0; i < numPurchases; i++) {
                // pick random category
                // Randomly pick a merchant
                String merchantPurchase = allMerchants.get(rand.nextInt(allMerchants.size()));

                // Find its category
                String categoryPurchase = categoryMerchants.entrySet()
                        .stream()
                        .filter(e -> e.getValue().contains(merchant))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("Other");

                Map<String, Object> merchantData = Map.of(
                    "name", merchantPurchase,
                    "category", categoryPurchase
                );

                ResponseEntity<Map> merchantResp = restTemplate.postForEntity(
                    apiConfig.getNessieBaseUrl() + "/merchants" + apiKey,
                    merchantData,
                    Map.class
                );

                Map<String, Object> createdMerchant = (Map<String, Object>) merchantResp.getBody().get("objectCreated");
                String merchantId = (String) createdMerchant.get("_id");

                double amount = (double)((int)((rand.nextDouble() * 200 + 10) * 100)) / 100;
                int dayNum = rand.nextInt(27) + 1;
                String day = (dayNum < 10 ? "0" : "") + dayNum;

                Map<String, Object> purchaseData = Map.of(
                    "merchant_id", merchantId,
                    "purchase_date", "2025-0" + (rand.nextInt(9) + 1) + "-" + day,
                    "amount", amount,
                    "status", "completed",
                    "medium", "balance"
                );

                ResponseEntity<Map> purchaseResp = restTemplate.postForEntity(
                    apiConfig.getNessieBaseUrl() + "/accounts/" + acc.getId() + "/purchases" + apiKey,
                    purchaseData,
                    Map.class
                );

                Purchase purchase = new Purchase();
                Map<String, Object> createdPurchase = (Map<String, Object>) purchaseResp.getBody().get("objectCreated");
                purchase.setId((String) createdPurchase.get("_id"));
                purchase.setMerchantName(merchant);
                purchase.setCategory(category);
                purchase.setPurchaseDate((String) purchaseData.get("purchase_date"));
                purchase.setAmount(amount);
                purchase.setAccountId(acc.getId());
                purchases.add(purchase);
            }
        }

        // === 5. Build the Customer profile ===
        Customer newProfile = new Customer();
        newProfile.setCustomerId(newCustomerId);
        newProfile.setName(selectedCustomer.get("first_name") + " " + selectedCustomer.get("last_name"));
        newProfile.setAccounts(accounts);

        // CustomerTransactions transactions = new CustomerTransactions();
        transactions.setDeposits(deposits);
        transactions.setWithdrawals(withdrawals);
        transactions.setBills(bills);
        transactions.setPurchases(purchases);
        newProfile.setTransactions(transactions);

        System.out.println("Created " + accounts.size() + " accounts, " + deposits.size() + " deposits, " + withdrawals.size() + " withdrawals, " + bills.size() + " bills, " + purchases.size() + " purchases for " + newCustomerId);

        return newProfile;
    }

    // Helper to fetch enterprise endpoint
    private List<Map<String, Object>> fetchEnterpriseEndpoint(String endpoint) {
        ResponseEntity<Map> resp = restTemplate.exchange(
                apiConfig.getNessieBaseUrl() + "/enterprise/" + endpoint + apiKey,
                HttpMethod.GET, null, Map.class
        );
        return (List<Map<String, Object>>) resp.getBody().get("results");
    }

    // provides a random subset of the provided list
    private List<Map<String, Object>> randomSubset(List<Map<String, Object>> source, int count) {
        Random rand = new Random();
        List<Map<String, Object>> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(count, copy.size()));
    }

    /**
     * Returns all the customer transactions (deposits, bills, withdrawals)
     * 
     * @param customerId The ID of the customer we want to get the transactions of 
     * @return Transactions of the customer
     */
    // public CustomerTransactions getTransactions(String customerId) {
    //     // fetch accounts
    //     String accountsUrl = apiConfig.getNessieBaseUrl() + "/customers/" + customerId + "/accounts" + apiKey;
    //     ResponseEntity<List<Account>> accountResponse = restTemplate.exchange(
    //             accountsUrl,
    //             HttpMethod.GET,
    //             null,
    //             new ParameterizedTypeReference<List<Account>>() {}
    //     );
    //     List<Account> accounts = accountResponse.getBody();

    //     List<Deposit> deposits = new ArrayList<>();
    //     List<Withdrawal> withdrawals = new ArrayList<>();
    //     List<Bill> bills = new ArrayList<>();
    //     List<Purchase> purchases = new ArrayList<>();

    //     for (Account account : accounts) {
    //         String accId = account.getId();

    //         // Deposits
    //         ResponseEntity<List<Map<String, Object>>> depositResp = restTemplate.exchange(
    //                 apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/deposits" + apiKey,
    //                 HttpMethod.GET,
    //                 null,
    //                 new ParameterizedTypeReference<List<Map<String, Object>>>() {}
    //         );
    //         for (Map<String, Object> d : depositResp.getBody()) {
    //             Deposit deposit = new Deposit();
    //             deposit.setId((String) d.get("_id"));
    //             deposit.setAmount(((Number) d.get("amount")).doubleValue());
    //             deposit.setStatus((String) d.get("status"));
    //             deposit.setTransactionDate((String) d.get("transaction_date"));
    //             deposit.setId(accId);
    //             deposit.setType("deposit");
    //             deposits.add(deposit);
    //         }

    //         // Withdrawals
    //         ResponseEntity<List<Map<String, Object>>> withdrawalResp = restTemplate.exchange(
    //                 apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/withdrawals" + apiKey,
    //                 HttpMethod.GET,
    //                 null,
    //                 new ParameterizedTypeReference<List<Map<String, Object>>>() {}
    //         );
    //         for (Map<String, Object> w : withdrawalResp.getBody()) {
    //             Withdrawal withdrawal = new Withdrawal();
    //             withdrawal.setId((String) w.get("_id"));
    //             withdrawal.setAmount(((Number) w.get("amount")).doubleValue());
    //             withdrawal.setStatus((String) w.get("status"));
    //             withdrawal.setTransactionDate((String) w.get("transaction_date"));
    //             withdrawal.setMedium((String) w.getOrDefault("medium", "balance"));
    //             withdrawal.setAccountId(accId);
    //             withdrawals.add(withdrawal);
    //         }

    //         // Bills
    //         ResponseEntity<List<Map<String, Object>>> billsResp = restTemplate.exchange(
    //                 apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/bills" + apiKey,
    //                 HttpMethod.GET,
    //                 null,
    //                 new ParameterizedTypeReference<List<Map<String, Object>>>() {}
    //         );
    //         for (Map<String, Object> b : billsResp.getBody()) {
    //             Bill bill = new Bill();
    //             bill.setId((String) b.get("_id"));
    //             bill.setAmount(((Number) b.get("amount")).doubleValue());
    //             bill.setStatus((String) b.get("status"));
    //             bill.setPaymentDate((String) b.get("payment_date"));
    //             bill.setPayee((String) b.get("payee"));
    //             bill.setAccountId(accId);
    //             bills.add(bill);
    //         }

    //         // Purchases (if you have them)
    //         ResponseEntity<List<Map<String, Object>>> purchasesResp = restTemplate.exchange(
    //                 apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/purchases" + apiKey,
    //                 HttpMethod.GET,
    //                 null,
    //                 new ParameterizedTypeReference<List<Map<String, Object>>>() {}
    //         );
    //         for (Map<String, Object> p : purchasesResp.getBody()) {
    //             Purchase purchase = new Purchase();
    //             purchase.setId((String) p.get("_id"));
    //             purchase.setAmount(((Number) p.get("amount")).doubleValue());
    //             purchase.setPurchaseDate((String) p.get("purchase_date"));
    //             purchase.setMerchantName((String) p.get("merchant_name"));
    //             purchase.setCategory((String) p.get("category"));
    //             purchases.add(purchase);
    //         }
    //     }

    //     CustomerTransactions customerTransactions = new CustomerTransactions();
    //     customerTransactions.setDeposits(deposits);
    //     customerTransactions.setWithdrawals(withdrawals);
    //     customerTransactions.setBills(bills);
    //     customerTransactions.setPurchases(purchases);

    //     return customerTransactions;
    // }   
}