package com.banking.platform.service;

import com.banking.platform.domain.Account;
import com.banking.platform.domain.Transaction;
import com.banking.platform.domain.TransactionStatus;
import com.banking.platform.domain.TransactionType;
import com.banking.platform.dto.TransactionRequest;
import com.banking.platform.dto.TransferRequest;
import com.banking.platform.repository.AccountRepository;
import com.banking.platform.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Transactional
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

    @Transactional
    public void transfer(TransferRequest request){

        Account from = accountRepository.findByAccountNumber(request.getFromAccount())
                .orElseThrow(()-> new IllegalStateException("Source account not found"));

        Account to = accountRepository.findByAccountNumber(request.getToAccount())
                .orElseThrow(()-> new IllegalStateException("Target account not found"));

        if (from.getBalance().compareTo(request.getAmount())<0){
            throw new IllegalStateException("Insufficient Balance");
        }

        //account updating
        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));

        //storing debit history
        Transaction debitTx = new Transaction();
        debitTx.setAccount(from);
        debitTx.setType(TransactionType.DEBIT);
        debitTx.setAmount(request.getAmount());
        debitTx.setStatus(TransactionStatus.SUCCESS);
        debitTx.setCreatedAt(LocalDateTime.now());


        //storing credit history
        Transaction creditTx = new Transaction();
        creditTx.setAccount(to);
        creditTx.setType(TransactionType.CREDIT);
        creditTx.setAmount(request.getAmount());
        creditTx.setStatus(TransactionStatus.SUCCESS);
        creditTx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);

    }

    public Page<Transaction> getTransaction(
            String accountNumber, int page, int size){
        return transactionRepository.findByAccount_AccountNumber(
                accountNumber,
                PageRequest.of(page,size, Sort.by("createdAt").descending())
        );
    }
}
