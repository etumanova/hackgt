package com.prospero.budget;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
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