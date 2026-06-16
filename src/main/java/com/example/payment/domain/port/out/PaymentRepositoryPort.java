package com.example.payment.domain.port.out;

import com.example.payment.domain.model.Payment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentRepositoryPort {
    Mono<Payment> save(Payment payment);
    Mono<Payment> findById(UUID paymentId);
    Flux<Payment> findByMerchantId(String merchantId);
}
