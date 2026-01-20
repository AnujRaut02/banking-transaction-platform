package com.banking.platform.event;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MoneyTransferredEvent {

    private final String fromAccount;
    private final String toAccount;
    private final BigDecimal amount;

    public MoneyTransferredEvent(String fromAccount, String toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }
}
