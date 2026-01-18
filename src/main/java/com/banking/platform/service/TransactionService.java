package com.banking.platform.service;

import com.banking.platform.domain.Account;
import com.banking.platform.domain.Transaction;
import com.banking.platform.domain.TransactionStatus;
import com.banking.platform.domain.TransactionType;
import com.banking.platform.dto.TransactionRequest;
import com.banking.platform.repository.AccountRepository;
import com.banking.platform.repository.TransactionRepository;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;


    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void credit(TransactionRequest request){

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(()-> new IllegalStateException("Account not found"));

        account.setBalance(account.getBalance().add(request.getAmount()));

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.CREDIT);
        tx.setAmount(request.getAmount());
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);

    }

    public void debit(TransactionRequest request){
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(()-> new IllegalStateException("Account not found"));

        if (account.getBalance().compareTo(request.getAmount())<0){
            throw new IllegalStateException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        Transaction tx =new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.DEBIT);
        tx.setAmount(request.getAmount());
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);
    }
}
