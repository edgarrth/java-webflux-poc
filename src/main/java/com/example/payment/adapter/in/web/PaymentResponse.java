package com.example.payment.adapter.in.web;

import com.example.payment.domain.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String merchantId,
        String customerId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {
    static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.paymentId(), payment.merchantId(), payment.customerId(), payment.amount(), payment.currency(), payment.status().name(), payment.createdAt());
    }
}
