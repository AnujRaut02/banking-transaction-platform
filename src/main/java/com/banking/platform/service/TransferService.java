package com.banking.platform.service;

import com.banking.platform.domain.Account;
import com.banking.platform.domain.IdempotencyKey;
import com.banking.platform.dto.TransferRequest;
import com.banking.platform.domain.event.TransferCompletedEvent;
import com.banking.platform.message.producer.TransferEventProducer;
import com.banking.platform.repository.IdempotencyKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class TransferService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransferEventProducer eventProducer;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public TransferService(AccountService accountService, TransactionService transactionService, TransferEventProducer eventProducer, IdempotencyKeyRepository idempotencyKeyRepository) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.eventProducer = eventProducer;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional
    @CacheEvict(value = "transactions",  allEntries = true, beforeInvocation = false)
    public void transfer(String idempotencyKey, TransferRequest request){

        if (idempotencyKeyRepository.existsByKey(idempotencyKey)){
            log.info("Duplicate transfer ignored for key={}", idempotencyKey);
            return;
        }

        idempotencyKeyRepository.save(
                new IdempotencyKey(null,idempotencyKey,Instant.now())
        );

        Account from = accountService.getByAccountNumber(request.getFromAccount());
        Account to = accountService.getByAccountNumber(request.getToAccount());

        accountService.debit(from,request.getAmount());
        accountService.credit(to,request.getAmount());

        transactionService.recordDebit(from,request.getAmount());
        transactionService.recordCredit(to,request.getAmount());

        TransferCompletedEvent event = TransferCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .transferId(UUID.randomUUID().toString())
                .fromAccount(from.getAccountNumber())
                .toAccount(to.getAccountNumber())
                .amount(request.getAmount())
                .currency("INR")
                .timestamp(Instant.now())
                .build();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        try {
                            eventProducer.publish(event);
                        } catch (Exception e) {
                            log.error(
                                    "Kafka publish failed for transferId={}",
                                    event.getTransferId(),
                                    e
                            );
                        }

                    }
                }
        );
    }
}
