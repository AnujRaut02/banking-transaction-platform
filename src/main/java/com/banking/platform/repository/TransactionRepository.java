package com.banking.platform.repository;

import com.banking.platform.domain.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        Page<Transaction> findByAccount_AccountNumber(String accountNumber, Pageable pageable);
}
