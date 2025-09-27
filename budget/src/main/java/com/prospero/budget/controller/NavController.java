package com.prospero.budget.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class NavController {
    @GetMapping("/")
    public String home() {
        return "index";  // resolves to templates/index.html
    }

    @GetMapping("/budgeter")
    public String budgeter() {
        return "budgeter";  // resolves to templates/budgeter.html
    }

    @GetMapping("/invest")
    public String invest() {
        return "invest";  // resolves to templates/invest.html
    }
}
