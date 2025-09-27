package com.prospero.budget.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private String id;
    private String nickname;
    private double balance;
    // type of account
    private String type;
}
