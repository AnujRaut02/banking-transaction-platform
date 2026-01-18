package com.banking.platform.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateAccountRequest {

    @NotBlank
    private String accountNumber;

}
