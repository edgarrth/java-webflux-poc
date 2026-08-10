package com.example.payment.adapter.out.persistence;

import com.example.payment.domain.model.Payment;

final class PaymentMapper {
    private PaymentMapper() {}

    static PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(
                payment.paymentId(),
                payment.merchantId(),
                payment.customerId(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                payment.createdAt()
        );
    }

    static Payment toDomain(PaymentEntity entity) {
        return Payment.rehydrate(
                entity.paymentId(),
                entity.merchantId(),
                entity.customerId(),
                entity.amount(),
                entity.currency(),
                entity.status(),
                entity.createdAt()
        );
    }
}
