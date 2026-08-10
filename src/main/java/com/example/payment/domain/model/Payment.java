package com.example.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Payment {
    private final UUID paymentId;
    private final String merchantId;
    private final String customerId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentStatus status;
    private final Instant createdAt;

    private Payment(UUID paymentId, String merchantId, String customerId, BigDecimal amount, String currency,
                    PaymentStatus status, Instant createdAt) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.merchantId = requireText(merchantId, "merchantId");
        this.customerId = requireText(customerId, "customerId");
        this.amount = requirePositive(amount);
        this.currency = requireCurrency(currency);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Payment receive(String merchantId, String customerId, BigDecimal amount, String currency) {
        return new Payment(UUID.randomUUID(), merchantId, customerId, amount, currency, PaymentStatus.RECEIVED, Instant.now());
    }

    /**
     * Rehydrates an aggregate previously persisted by an infrastructure adapter.
     */
    public static Payment rehydrate(UUID paymentId, String merchantId, String customerId, BigDecimal amount,
                                    String currency, PaymentStatus status, Instant createdAt) {
        return new Payment(paymentId, merchantId, customerId, amount, currency, status, createdAt);
    }

    public Payment authorize() {
        requireStatus(PaymentStatus.RECEIVED, "Only RECEIVED payments can be authorized");
        return withStatus(PaymentStatus.AUTHORIZED);
    }

    public Payment reject() {
        requireStatus(PaymentStatus.RECEIVED, "Only RECEIVED payments can be rejected");
        return withStatus(PaymentStatus.REJECTED);
    }

    public Payment settle() {
        requireStatus(PaymentStatus.AUTHORIZED, "Only AUTHORIZED payments can be settled");
        return withStatus(PaymentStatus.SETTLED);
    }

    private Payment withStatus(PaymentStatus newStatus) {
        return new Payment(paymentId, merchantId, customerId, amount, currency, newStatus, createdAt);
    }

    private void requireStatus(PaymentStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static BigDecimal requirePositive(BigDecimal value) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
        return value;
    }

    private static String requireCurrency(String value) {
        if (value == null || !value.matches("[A-Z]{3}")) throw new IllegalArgumentException("currency must be ISO-4217");
        return value;
    }

    public UUID paymentId() { return paymentId; }
    public String merchantId() { return merchantId; }
    public String customerId() { return customerId; }
    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    public PaymentStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
}
