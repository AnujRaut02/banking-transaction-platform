package com.banking.platform.transfer.saga;

import com.banking.platform.common.idempotency.EventIdempotencyGuard;
import com.banking.platform.transfer.domain.Transfer;
import com.banking.platform.transfer.domain.TransferStatus;
import com.banking.platform.transfer.event.CreditRequestedEvent;
import com.banking.platform.transfer.event.DebitCompletedEvent;
import com.banking.platform.transfer.observability.TransferMetrics;
import com.banking.platform.transfer.persistence.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Slf4j
@Component
public class DebitCompletedListener {

    private final TransferRepository transferRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventIdempotencyGuard idempotencyGuard;


    public DebitCompletedListener(
            TransferRepository transferRepository,
            KafkaTemplate<String, Object> kafkaTemplate, TransferMetrics metrics, EventIdempotencyGuard idempotencyGuard
    ) {
        this.transferRepository = transferRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.idempotencyGuard = idempotencyGuard;
    }

    @KafkaListener(topics = "debit.completed")
    @Transactional
    public void onDebitCompleted(
            DebitCompletedEvent event,
            Acknowledgment ack
    ) {

        if (idempotencyGuard.alreadyProcessed(
                event.eventId()
        )) {
            ack.acknowledge();
            return;
        }
        log.error("debit listener ACTIVE {}", this);
        Transfer transfer = transferRepository
                .findById(event.transferId())
                .orElseThrow();

        if (transfer.getStatus() != TransferStatus.CREDIT_IN_PROGRESS) {
            ack.acknowledge();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        kafkaTemplate.send(
                                "credit.requested",
                                event.transferId().toString(),
                                new CreditRequestedEvent(
                                        UUID.randomUUID().toString(),
                                        event.transferId(),
                                        transfer.getToAccountId(),
                                        transfer.getAmount()
                                )
                        );
                        ack.acknowledge();
                    }
                }
        );
    }


}

