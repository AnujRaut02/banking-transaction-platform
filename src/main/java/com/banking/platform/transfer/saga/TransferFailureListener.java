package com.banking.platform.transfer.saga;

import com.banking.platform.transfer.domain.Transfer;
import com.banking.platform.transfer.domain.TransferStatus;
import com.banking.platform.transfer.event.TransferRequestedEvent;
import com.banking.platform.transfer.persistence.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferFailureListener {

    private final TransferRepository transferRepository;

    @KafkaListener(topics = "transfer.requested.DLT")
    @Transactional
    public void onTransferFailed(
            TransferRequestedEvent event,
            Acknowledgment ack
    ) {
        Transfer transfer = transferRepository
                .findById(event.transferId())
                .orElse(null);

        if (transfer == null) {
            ack.acknowledge();
            return;
        }

        // Never to override a completed transfer
        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            ack.acknowledge();
            return;
        }

        if (transfer.getStatus() == TransferStatus.PENDING
                || transfer.getStatus() == TransferStatus.DEBIT_IN_PROGRESS
                || transfer.getStatus() == TransferStatus.CREDIT_IN_PROGRESS) {

            transfer.transitionTo(TransferStatus.FAILED);
            transferRepository.save(transfer);
        }

        ack.acknowledge();
    }

}
