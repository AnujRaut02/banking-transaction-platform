package com.banking.platform.domain.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transferId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private Instant occurredAt;
}
