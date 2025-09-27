package com.prospero.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.prospero.budget")
public class BudgetApplication {
	public static void main(String[] args) {
		SpringApplication.run(BudgetApplication.class, args);
	}
}