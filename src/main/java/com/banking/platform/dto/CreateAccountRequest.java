package com.banking.platform.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CreateAccountRequest {

    @NotBlank
    private String accountNumber;

    @NotNull
    private UUID customerId;
}
