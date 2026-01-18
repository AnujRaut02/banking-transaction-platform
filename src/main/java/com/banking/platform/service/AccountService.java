package com.banking.platform.service;

import com.banking.platform.domain.Account;
import com.banking.platform.domain.AccountStatus;
import com.banking.platform.dto.AccountResponse;
import com.banking.platform.dto.CreateAccountRequest;
import com.banking.platform.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;


    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request){
        accountRepository.findByAccountNumber(request.getAccountNumber())
                .ifPresent(a->{
                    throw new IllegalStateException("Account already exists");
                });

        Account account= new Account();
        account.setAccountNumber(request.getAccountNumber());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);

        Account saved = accountRepository.save(account);

        AccountResponse response = new AccountResponse();
        response.setAccountNumber(saved.getAccountNumber());
        response.setBalance(saved.getBalance());
        response.setStatus(saved.getStatus().name());

        return response;

    }
}
