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

    @Autowired
    public NessieService(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.apiKey = "?key=" + apiConfig.getNessieApiKey();
    }

    public Customer initializeSampleCustomer() {
        // this imports all of the enterprise customers, chooses a random customer, and then finds the 
        // set of all the accounts and transactions (deposits, bills, withdrawals)
        return importAndCloneEnterpriseCustomer();
    }

    private Customer importAndCloneEnterpriseCustomer() {
        Random rand = new Random();

        // get enterprise customers and pick a random one 
        String enterpriseCustomerUrl = apiConfig.getNessieBaseUrl() + "/enterprise/customers" + apiKey;
        ResponseEntity<Map> customerResponse = restTemplate.exchange(
                enterpriseCustomerUrl, HttpMethod.GET, null, Map.class
        );
        List<Map<String, Object>> customers = (List<Map<String, Object>>) customerResponse.getBody().get("results");
        Map<String, Object> selectedCustomer = customers.get(rand.nextInt(customers.size()));
        String customerId = (String) selectedCustomer.get("_id");

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        System.out.println("Selected customer: " + selectedCustomer.get("_id"));

        // get all enterprise accounts for that random customer
        String enterpriseAccountsUrl = apiConfig.getNessieBaseUrl() + "/enterprise/accounts" + apiKey;
        ResponseEntity<Map> accountsResponse = restTemplate.exchange(
                enterpriseAccountsUrl, HttpMethod.GET, null, Map.class
        );
        List<Map<String, Object>> allAccounts = (List<Map<String, Object>>) accountsResponse.getBody().get("results");
        List<Map<String, Object>> customerAccounts = allAccounts.stream()
                .filter(a -> a.get("customer_id").equals(customerId))
                .collect(Collectors.toList());

        // fetch enterprise deposits, withdrawals, bills
        List<Map<String, Object>> allDeposits = fetchEnterpriseEndpoint("deposits");
        List<Map<String, Object>> allWithdrawals = fetchEnterpriseEndpoint("withdrawals");
        List<Map<String, Object>> allBills = fetchEnterpriseEndpoint("bills");

        // CREATE NEW CUSTOMER!
        Map<String, Object> newCustomerMap = Map.of(
                //"_id", customerId,
                "first_name", selectedCustomer.get("first_name"),
                "last_name", selectedCustomer.get("last_name"),
                "address", selectedCustomer.get("address")
        );
        ResponseEntity<Map> createdCustomer = restTemplate.postForEntity(
                apiConfig.getNessieBaseUrl() + "/customers" + apiKey, newCustomerMap, Map.class
        );
        String newCustomerId = (String) createdCustomer.getBody().get("_id");

        // clone accounts for customer
        List<Account> clonedAccounts = new ArrayList<>();
        for (Map<String, Object> acc : customerAccounts) {
            Map<String, Object> accMap = Map.of(
                //"_id", acc.get("_id"),
                "type", acc.get("type"),
                "nickname", acc.get("nickname"),
                "rewards", acc.getOrDefault("rewards", 0),
                "balance", acc.get("balance"),
                "customer_id", newCustomerId
            );
            ResponseEntity<Map> accResp = restTemplate.postForEntity(
                    apiConfig.getNessieBaseUrl() + "/accounts" + apiKey, accMap, Map.class
            );

            Account account = new Account();
            account.setId((String) accResp.getBody().get("_id"));
            account.setType((String) acc.get("type"));
            account.setNickname((String) acc.get("nickname"));
            account.setBalance(((Number) acc.get("balance")).doubleValue());
            clonedAccounts.add(account);
        }

        // clone deposits
        List<Deposit> deposits = clonedAccounts.stream()
                .flatMap(account -> allDeposits.stream()
                        .filter(d -> d.get("account_id").equals(getOriginalAccountId(customerAccounts, account)))
                        .map(d -> {
                            Map<String, Object> depositMap = Map.of(
                                    "medium", d.getOrDefault("medium", "balance"),
                                    "transaction_date", d.get("transaction_date"),
                                    "status", d.get("status"),
                                    "amount", d.get("amount"),
                                    "payee_id", account.getId()
                            );
                            ResponseEntity<Map> depResp = restTemplate.postForEntity(
                                    apiConfig.getNessieBaseUrl() + "/deposits" + apiKey, depositMap, Map.class
                            );
                            Deposit dep = new Deposit();
                            dep.setId((String) depResp.getBody().get("_id"));
                            dep.setAmount(((Number) d.get("amount")).doubleValue());
                            dep.setTransactionDate((String) d.get("transaction_date"));
                            dep.setStatus((String) d.get("status"));
                            dep.setType("deposit");
                            return dep;
                        })
                ).collect(Collectors.toList());

        // clone withdrawals
        List<Withdrawal> withdrawals = clonedAccounts.stream()
                .flatMap(account -> allWithdrawals.stream()
                        .filter(w -> w.get("account_id").equals(getOriginalAccountId(customerAccounts, account)))
                        .map(w -> {
                            Withdrawal withdrawal = new Withdrawal();
                            withdrawal.setId((String) w.get("_id"));
                            withdrawal.setAccountId(account.getId());
                            withdrawal.setAmount(((Number) w.get("amount")).doubleValue());
                            withdrawal.setTransactionDate((String) w.get("transaction_date"));
                            withdrawal.setStatus((String) w.get("status"));
                            withdrawal.setMedium((String) w.getOrDefault("medium", "balance"));
                            return withdrawal;
                        })
                ).collect(Collectors.toList());

        // clone bills
        List<Bill> bills = clonedAccounts.stream()
                .flatMap(account -> allBills.stream()
                        .filter(b -> b.get("account_id").equals(getOriginalAccountId(customerAccounts, account)))
                        .map(b -> {
                            Bill bill = new Bill();
                            bill.setId((String) b.get("_id"));
                            bill.setAccountId(account.getId());
                            bill.setAmount(((Number) b.get("amount")).doubleValue());
                            bill.setPaymentDate((String) b.get("payment_date"));
                            bill.setPayee((String) b.get("payee"));
                            bill.setStatus((String) b.get("status"));
                            return bill;
                        })
                ).collect(Collectors.toList());

        // build the customer profile
        Customer profile = new Customer();
        profile.setCustomerId(newCustomerId);
        profile.setName(selectedCustomer.get("first_name") + " " + selectedCustomer.get("last_name"));
        profile.setAccounts(clonedAccounts);

        CustomerTransactions transactions = new CustomerTransactions();
        transactions.setDeposits(deposits);
        transactions.setWithdrawals(withdrawals);
        transactions.setBills(bills);
        //transactions.setPurchases(purchases);

        profile.setTransactions(transactions);

        return profile;
    }

    // Helper to fetch enterprise endpoint
    private List<Map<String, Object>> fetchEnterpriseEndpoint(String endpoint) {
        ResponseEntity<Map> resp = restTemplate.exchange(
                apiConfig.getNessieBaseUrl() + "/enterprise/" + endpoint + apiKey,
                HttpMethod.GET, null, Map.class
        );
        return (List<Map<String, Object>>) resp.getBody().get("results");
    }

    /**
     * Helper method to get original account ID from the nickname
     * 
     * @param originalAccounts The list of original accounts
     * @param account The account object
     * @return Returns ID of original account if the nickname exists
     */
    private String getOriginalAccountId(List<Map<String, Object>> originalAccounts, Account account) {
        return originalAccounts.stream()
                .filter(a -> a.get("nickname").equals(account.getNickname()))
                .findFirst()
                .map(a -> (String) a.get("_id"))
                .orElse(null);
    }

    /**
     * Returns all the customer transactions (deposits, bills, withdrawals)
     * 
     * @param customerId The ID of the customer we want to get the transactions of 
     * @return Transactions of the customer
     */
    public CustomerTransactions getTransactions(String customerId) {
        // fetch accounts
        String accountsUrl = apiConfig.getNessieBaseUrl() + "/customers/" + customerId + "/accounts" + apiKey;
        ResponseEntity<List<Account>> accountResponse = restTemplate.exchange(
                accountsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Account>>() {}
        );
        List<Account> accounts = accountResponse.getBody();

        List<Deposit> deposits = new ArrayList<>();
        List<Withdrawal> withdrawals = new ArrayList<>();
        List<Bill> bills = new ArrayList<>();
        List<Purchase> purchases = new ArrayList<>();

        for (Account account : accounts) {
            String accId = account.getId();

            // Deposits
            ResponseEntity<List<Map<String, Object>>> depositResp = restTemplate.exchange(
                    apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/deposits" + apiKey,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            for (Map<String, Object> d : depositResp.getBody()) {
                Deposit deposit = new Deposit();
                deposit.setId((String) d.get("_id"));
                deposit.setAmount(((Number) d.get("amount")).doubleValue());
                deposit.setStatus((String) d.get("status"));
                deposit.setTransactionDate((String) d.get("transaction_date"));
                deposit.setId(accId);
                deposit.setType("deposit");
                deposits.add(deposit);
            }

            // Withdrawals
            ResponseEntity<List<Map<String, Object>>> withdrawalResp = restTemplate.exchange(
                    apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/withdrawals" + apiKey,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            for (Map<String, Object> w : withdrawalResp.getBody()) {
                Withdrawal withdrawal = new Withdrawal();
                withdrawal.setId((String) w.get("_id"));
                withdrawal.setAmount(((Number) w.get("amount")).doubleValue());
                withdrawal.setStatus((String) w.get("status"));
                withdrawal.setTransactionDate((String) w.get("transaction_date"));
                withdrawal.setMedium((String) w.getOrDefault("medium", "balance"));
                withdrawal.setAccountId(accId);
                withdrawals.add(withdrawal);
            }

            // Bills
            ResponseEntity<List<Map<String, Object>>> billsResp = restTemplate.exchange(
                    apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/bills" + apiKey,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            for (Map<String, Object> b : billsResp.getBody()) {
                Bill bill = new Bill();
                bill.setId((String) b.get("_id"));
                bill.setAmount(((Number) b.get("amount")).doubleValue());
                bill.setStatus((String) b.get("status"));
                bill.setPaymentDate((String) b.get("payment_date"));
                bill.setPayee((String) b.get("payee"));
                bill.setAccountId(accId);
                bills.add(bill);
            }

            // Purchases (if you have them)
            ResponseEntity<List<Map<String, Object>>> purchasesResp = restTemplate.exchange(
                    apiConfig.getNessieBaseUrl() + "/accounts/" + accId + "/purchases" + apiKey,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            for (Map<String, Object> p : purchasesResp.getBody()) {
                Purchase purchase = new Purchase();
                purchase.setId((String) p.get("_id"));
                purchase.setAmount(((Number) p.get("amount")).doubleValue());
                purchase.setPurchaseDate((String) p.get("purchase_date"));
                purchase.setMerchantName((String) p.get("merchant_name"));
                purchase.setCategory("purchase (FIGURE OUT HOW TO CATEGORIZE)");
                purchases.add(purchase);
            }
        }

        CustomerTransactions customerTransactions = new CustomerTransactions();
        customerTransactions.setDeposits(deposits);
        customerTransactions.setWithdrawals(withdrawals);
        customerTransactions.setBills(bills);
        customerTransactions.setPurchases(purchases);

        return customerTransactions;
    }   
}