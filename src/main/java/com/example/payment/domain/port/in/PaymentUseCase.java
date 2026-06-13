package com.example.payment.domain.port.in;

import com.example.payment.domain.model.Payment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentUseCase {
    Mono<Payment> createPayment(String merchantId, String customerId, BigDecimal amount, String currency);
    Mono<Payment> authorizePayment(UUID paymentId);
    Mono<Payment> settlePayment(UUID paymentId);
    Mono<Payment> getPayment(UUID paymentId);
    Flux<Payment> streamPaymentsByMerchant(String merchantId);
    Flux<Payment> authorizeBatch(Flux<UUID> paymentIds);
}
