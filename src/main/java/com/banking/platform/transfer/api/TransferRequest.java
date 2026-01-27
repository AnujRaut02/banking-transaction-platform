package com.banking.platform.transfer.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransferRequest {

    @NotNull
    private String fromAccount;

    @NotNull
    private String toAccount;

    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

}
