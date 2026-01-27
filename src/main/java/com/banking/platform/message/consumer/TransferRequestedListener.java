package com.banking.platform.message.consumer;

import com.banking.platform.common.idempotency.EventIdempotencyGuard;
import com.banking.platform.domain.Account;
import com.banking.platform.repository.AccountRepository;
import com.banking.platform.transfer.domain.Transfer;
import com.banking.platform.transfer.domain.TransferStatus;
import com.banking.platform.transfer.event.DebitCompletedEvent;
import com.banking.platform.transfer.event.TransferRequestedEvent;
import com.banking.platform.transfer.persistence.TransferRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Slf4j
@Component
public class TransferRequestedListener {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventIdempotencyGuard idempotencyGuard;

    public TransferRequestedListener(
            AccountRepository accountRepository,
            TransferRepository transferRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            EventIdempotencyGuard idempotencyGuard
    ) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.idempotencyGuard = idempotencyGuard;
    }


    @KafkaListener(topics = "transfer.requested")
    @Transactional
    public void onTransferRequested(
            TransferRequestedEvent event,
            Acknowledgment ack
    ) {
        try {

            if (idempotencyGuard.alreadyProcessed(event.eventId().toString())) {
                return;
            }
            log.error("🔥 TransferRequestedListener ACTIVE {}", this);
            Transfer transfer = transferRepository
                    .findById(event.transferId())
                    .orElse(null);

            if (transfer == null || transfer.getStatus() != TransferStatus.PENDING) {
                return;
            }

            Account account = accountRepository
                    .findByIdForUpdate(event.fromAccountId())
                    .orElse(null);

            if (account == null || account.getBalance().compareTo(event.amount()) < 0) {
                transfer.transitionTo(TransferStatus.FAILED);
                idempotencyGuard.markProcessed(event.eventId().toString(), "TRANSFER_REQUESTED");
                return;
            }

            transfer.transitionTo(TransferStatus.DEBIT_IN_PROGRESS);
            account.debit(event.amount());
            transfer.transitionTo(TransferStatus.CREDIT_IN_PROGRESS);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            kafkaTemplate.send(
                                    "debit.completed",
                                    transfer.getId().toString(),
                                    new DebitCompletedEvent(
                                            UUID.randomUUID().toString(),
                                            transfer.getId(),
                                            event.fromAccountId(),
                                            event.amount()
                                    )
                            );
                        }
                    }
            );

            idempotencyGuard.markProcessed(event.eventId().toString(), "TRANSFER_REQUESTED");

        } finally {
            ack.acknowledge(); // ✅ EXACTLY ONCE
        }
    }



}
