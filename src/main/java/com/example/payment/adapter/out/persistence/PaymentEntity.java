package com.example.payment.adapter.out.persistence;

import com.example.payment.domain.model.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("payments")
public record PaymentEntity(
        @Id UUID paymentId,
        String merchantId,
        String customerId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant createdAt
) { }
