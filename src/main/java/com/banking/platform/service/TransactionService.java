package com.banking.platform.service;

import com.banking.platform.domain.Account;
import com.banking.platform.domain.Transaction;
import com.banking.platform.domain.TransactionStatus;
import com.banking.platform.domain.TransactionType;
import com.banking.platform.dto.TransactionRequest;
import com.banking.platform.dto.TransactionResponse;
import com.banking.platform.repository.TransactionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }


    public void recordCredit(Account account, BigDecimal amount){
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.CREDIT);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    public void recordDebit(Account account, BigDecimal amount){
        Transaction tx =new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.DEBIT);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }


    @Transactional
    @CacheEvict(value = "transactions", allEntries = true)
    public void debit(TransactionRequest request){

        Account account = accountService.getByAccountNumber(request.getAccountNumber());

        accountService.debit(account,request.getAmount());

        recordDebit(account,request.getAmount());

    }

    @Transactional
    @CacheEvict(value = "transactions", allEntries = true)
    public void credit(TransactionRequest request){

        Account account = accountService.getByAccountNumber(request.getAccountNumber());

        accountService.credit(account,request.getAmount());

        recordCredit(account,request.getAmount());

    }



    @Cacheable(
            value = "transactions:v1",
            key = "T(String).format('%s:%d:%d', #accountNumber, #page, #size)",
            unless = "#result.isEmpty()"
    )
    public List<TransactionResponse> getTransaction(String accountNumber, int page, int size){

        Pageable pageable= PageRequest.of(page, size, Sort.by("createdAt").descending());
        System.out.println(">>> DB HIT 2<<<");
        return transactionRepository.findByAccount_AccountNumber(accountNumber,pageable)
                .map(tx -> new TransactionResponse(
                        tx.getAmount(),
                        tx.getType().name(),
                        tx.getStatus().name(),
                        tx.getCreatedAt()
                )).getContent();

    }
}
