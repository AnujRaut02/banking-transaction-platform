package com.banking.platform.transfer.event;

import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferCompletedEvent {

    private String eventId;
    private String transferId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private Instant timestamp;

}

