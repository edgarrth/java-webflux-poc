package com.example.payment.adapter.out.persistence;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ReactivePaymentRepositoryAdapter implements PaymentRepositoryPort {
    private final R2dbcPaymentRepository repository;

    public ReactivePaymentRepositoryAdapter(R2dbcPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Payment> save(Payment payment) {
        return repository.save(PaymentMapper.toEntity(payment)).map(PaymentMapper::toDomain);
    }

    @Override
    public Mono<Payment> findById(UUID paymentId) {
        return repository.findById(paymentId).map(PaymentMapper::toDomain);
    }

    @Override
    public Flux<Payment> findByMerchantId(String merchantId) {
        return repository.findByMerchantId(merchantId).map(PaymentMapper::toDomain);
    }
}
