package com.banking.platform.transfer.event;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitCompletedEvent(
        String eventId,
        UUID transferId,
        Long fromAccountId,
        BigDecimal amount
) {}


