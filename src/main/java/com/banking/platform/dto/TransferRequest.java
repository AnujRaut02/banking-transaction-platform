package com.banking.platform.dto;

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

    @Positive
    private BigDecimal amount;

}
