package com.example.payment.adapter.out.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface R2dbcPaymentRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {
    Flux<PaymentEntity> findByMerchantId(String merchantId);
}
