package com.banking.platform.repository;

import com.banking.platform.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey,Long> {

    boolean existsByKey(String key);
}
