package com.banking.platform.transfer.event;

import java.io.Serializable;
import java.util.UUID;

public record CompensateDebitEvent(
        String eventId,
        UUID transferId
) implements Serializable {
}
