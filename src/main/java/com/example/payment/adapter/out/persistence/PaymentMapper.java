package com.example.payment.adapter.out.persistence;

import com.example.payment.domain.model.Payment;

import java.lang.reflect.Constructor;

final class PaymentMapper {
    private PaymentMapper() {}

    static PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(payment.paymentId(), payment.merchantId(), payment.customerId(), payment.amount(), payment.currency(), payment.status(), payment.createdAt());
    }

    static Payment toDomain(PaymentEntity entity) {
        try {
            Constructor<Payment> constructor = Payment.class.getDeclaredConstructor(
                    java.util.UUID.class, String.class, String.class, java.math.BigDecimal.class,
                    String.class, com.example.payment.domain.model.PaymentStatus.class, java.time.Instant.class);
            constructor.setAccessible(true);
            return constructor.newInstance(entity.paymentId(), entity.merchantId(), entity.customerId(), entity.amount(), entity.currency(), entity.status(), entity.createdAt());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot map PaymentEntity to domain", e);
        }
    }
}
