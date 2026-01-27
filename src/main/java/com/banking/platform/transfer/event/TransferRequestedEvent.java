package com.banking.platform.transfer.event;


import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestedEvent(
        UUID eventId,
        UUID transferId,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount
) {}


