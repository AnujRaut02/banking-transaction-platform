package com.banking.platform.transfer.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record CreditRequestedEvent(
        String eventId,
        UUID transferId,
        Long toAccountId,
        BigDecimal amount
) implements Serializable {
}
