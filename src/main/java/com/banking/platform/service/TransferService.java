package com.banking.platform.service;

import com.banking.platform.domain.Account;
import com.banking.platform.dto.TransferRequest;
import com.banking.platform.event.MoneyTransferredEvent;
import org.springframework.cache.annotation.CacheEvict;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;

    public TransferService(AccountService accountService, TransactionService transactionService, ApplicationEventPublisher eventPublisher) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @CacheEvict(value = "transactions",  allEntries = true)
    public void transfer(TransferRequest request){

        Account from = accountService.getByAccountNumber(request.getFromAccount());
        Account to = accountService.getByAccountNumber(request.getToAccount());

        accountService.debit(from,request.getAmount());
        accountService.credit(to,request.getAmount());

        transactionService.recordDebit(from,request.getAmount());
        transactionService.recordCredit(to,request.getAmount());

        eventPublisher.publishEvent(new MoneyTransferredEvent(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount()));
    }
}
