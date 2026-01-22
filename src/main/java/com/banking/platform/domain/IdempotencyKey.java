package com.banking.platform.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DialectOverride;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys", uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String key;

    private Instant createdAt = Instant.now();
}
