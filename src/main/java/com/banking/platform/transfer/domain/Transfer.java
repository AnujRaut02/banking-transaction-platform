package com.banking.platform.transfer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Setter
@Getter
public class Transfer {

    @Id
    private UUID id;

    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private Instant createdAt;

    public void transitionTo(TransferStatus next) {

        if (!isValidTransition(this.status, next)) {
            throw new IllegalStateException(
                    "Illegal transfer state transition: " +
                            this.status + " → " + next
            );
        }

        this.status = next;
    }

    private boolean isValidTransition(TransferStatus from, TransferStatus to) {

        return switch (from) {

            case PENDING ->
                    to == TransferStatus.DEBIT_IN_PROGRESS
                            || to == TransferStatus.FAILED;

            case DEBIT_IN_PROGRESS ->
                    to == TransferStatus.CREDIT_IN_PROGRESS
                            || to == TransferStatus.FAILED;

            case CREDIT_IN_PROGRESS ->
                    to == TransferStatus.COMPLETED
                            || to == TransferStatus.FAILED;

            default -> false;
        };
    }


}