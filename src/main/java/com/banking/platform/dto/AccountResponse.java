package com.banking.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AccountResponse {

    private String accountNumber;
    private BigDecimal balance;
    private String status;

}
