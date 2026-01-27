package com.banking.platform.message.consumer;

import com.banking.platform.domain.audit.AuditLog;
import com.banking.platform.transfer.event.TransferCompletedEvent;
import com.banking.platform.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class AuditConsumer {

    private final AuditLogRepository auditLogRepository;

    public AuditConsumer(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @KafkaListener(
            topics = "transfer.events",
            groupId = "audit-group",
            autoStartup = "false"
    )
    @Transactional(readOnly = true)
    public void consume(TransferCompletedEvent event) {

        try {
            AuditLog logEntry = new AuditLog();
            logEntry.setTransferId(event.getTransferId());
            logEntry.setFromAccount(event.getFromAccount());
            logEntry.setToAccount(event.getToAccount());
            logEntry.setAmount(event.getAmount());
            logEntry.setCurrency(event.getCurrency());
            logEntry.setOccurredAt(event.getTimestamp());

            auditLogRepository.save(logEntry);

            log.info("Audit saved for transfer {}", event.getTransferId());

        } catch (Exception e) {

            log.error(
                    "Audit failed for transferId={}",
                    event.getTransferId(),
                    e
            );
        }
    }
}
