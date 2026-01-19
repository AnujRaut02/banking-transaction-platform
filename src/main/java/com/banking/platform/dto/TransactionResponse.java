package com.banking.platform.dto;

import com.banking.platform.domain.TransactionStatus;
import com.banking.platform.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(BigDecimal amount,
                                  String type,
                                  String status,
                                  LocalDateTime createdAt) {
}
