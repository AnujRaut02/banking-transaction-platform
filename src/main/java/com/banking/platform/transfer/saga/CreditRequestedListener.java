package com.banking.platform.transfer.saga;

import com.banking.platform.common.idempotency.EventIdempotencyGuard;
import com.banking.platform.domain.Account;
import com.banking.platform.repository.AccountRepository;
import com.banking.platform.transfer.domain.Transfer;
import com.banking.platform.transfer.domain.TransferStatus;
import com.banking.platform.transfer.event.CreditRequestedEvent;
import com.banking.platform.transfer.persistence.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class CreditRequestedListener {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;


    public CreditRequestedListener(TransferRepository transferRepository, AccountRepository accountRepository, EventIdempotencyGuard idempotencyGuard) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
    }

    @KafkaListener(topics = "credit.requested")
    @Transactional
    public void onCreditRequested(
            CreditRequestedEvent event,
            Acknowledgment ack
    ) {
        Transfer transfer = transferRepository
                .findById(event.transferId())
                .orElseThrow();

        //FINAL STATE GUARD
        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            ack.acknowledge();
            return;
        }

        if (transfer.getStatus() != TransferStatus.CREDIT_IN_PROGRESS) {
            ack.acknowledge();
            return;
        }

        Account toAccount = accountRepository
                .findByIdForUpdate(event.toAccountId())
                .orElseThrow();

        toAccount.credit(event.amount());
        accountRepository.save(toAccount);

        transfer.transitionTo(TransferStatus.COMPLETED);
        transferRepository.save(transfer);

        ack.acknowledge();
    }


}


