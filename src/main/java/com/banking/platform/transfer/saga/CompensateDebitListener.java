package com.banking.platform.transfer.saga;

import com.banking.platform.common.idempotency.EventIdempotencyGuard;
import com.banking.platform.domain.Account;
import com.banking.platform.repository.AccountRepository;
import com.banking.platform.transfer.domain.Transfer;
import com.banking.platform.transfer.domain.TransferStatus;
import com.banking.platform.transfer.event.CompensateDebitEvent;
import com.banking.platform.transfer.persistence.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class CompensateDebitListener {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final EventIdempotencyGuard idempotencyGuard;

    public CompensateDebitListener(TransferRepository transferRepository, AccountRepository accountRepository, EventIdempotencyGuard idempotencyGuard) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.idempotencyGuard = idempotencyGuard;
    }

    @KafkaListener(topics = "debit.compensate")
    @Transactional
    public void onCompensate(
            CompensateDebitEvent event,
            Acknowledgment ack
    ) {

        if (idempotencyGuard.alreadyProcessed(
                event.eventId()
        )) {
            ack.acknowledge();
            return;
        }

        Transfer transfer = transferRepository
                .findById(event.transferId())
                .orElseThrow();

        if (transfer.getStatus() != TransferStatus.FAILED) {
            ack.acknowledge();
            return;
        }

        Account account = accountRepository
                .findByIdForUpdate(transfer.getFromAccountId())
                .orElseThrow();

        account.credit(transfer.getAmount());
        accountRepository.save(account);

        ack.acknowledge();
    }
}
