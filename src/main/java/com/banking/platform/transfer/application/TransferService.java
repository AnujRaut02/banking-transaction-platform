package com.banking.platform.transfer.application;

import com.banking.platform.domain.IdempotencyKey;
import com.banking.platform.repository.AccountRepository;
import com.banking.platform.repository.IdempotencyKeyRepository;
import com.banking.platform.transfer.api.TransferRequest;
import com.banking.platform.transfer.domain.Transfer;
import com.banking.platform.transfer.domain.TransferStatus;
import com.banking.platform.transfer.event.TransferRequestedEvent;
import com.banking.platform.transfer.persistence.TransferRepository;
import com.banking.platform.message.producer.TransferRequestedEventProducer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class TransferService {

    private final TransferRequestedEventProducer eventProducer;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;


    @PersistenceContext
    private EntityManager entityManager;


    public TransferService(
            TransferRequestedEventProducer eventProducer,
            IdempotencyKeyRepository idempotencyKeyRepository,
            TransferRepository transferRepository,
            AccountRepository accountRepository
    ) {
        this.eventProducer = eventProducer;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @CacheEvict(value = "transactions", allEntries = true)
    public UUID initiateTransfer(String idempotencyKey, TransferRequest request) {

        if (idempotencyKeyRepository.existsByKey(idempotencyKey)) {
            throw new IllegalStateException("Duplicate request");
        }

        Long fromAccountId = accountRepository
                .findIdByAccountNumber(request.getFromAccount())
                .orElseThrow();

        Long toAccountId = accountRepository
                .findIdByAccountNumber(request.getToAccount())
                .orElseThrow();

        idempotencyKeyRepository.save(
                new IdempotencyKey(null, idempotencyKey, Instant.now())
        );

        Transfer transfer = new Transfer();
        transfer.setId(UUID.randomUUID());
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setAmount(request.getAmount());
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setCreatedAt(Instant.now());

        transferRepository.save(transfer);
        entityManager.detach(transfer);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        try {
                            eventProducer.publish(
                                    new TransferRequestedEvent(
                                            UUID.randomUUID(),
                                            transfer.getId(),
                                            fromAccountId,
                                            toAccountId,
                                            request.getAmount()
                                    )
                            );
                        } catch (Exception e) {
                            log.error(
                                    "Post-commit Kafka publish failed for transferId={}",
                                    transfer.getId(),
                                    e
                            );
                        }
                    }
                }
        );

        return transfer.getId();
    }

}

