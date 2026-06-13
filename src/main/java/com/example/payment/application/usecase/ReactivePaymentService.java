package com.example.payment.application.usecase;

import com.example.payment.application.service.PaymentRiskPolicy;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.in.PaymentUseCase;
import com.example.payment.domain.port.out.PaymentEventPublisherPort;
import com.example.payment.domain.port.out.PaymentRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Service
public class ReactivePaymentService implements PaymentUseCase {
    private final PaymentRepositoryPort repository;
    private final PaymentEventPublisherPort events;
    private final PaymentRiskPolicy riskPolicy;
    private final int maxConcurrency;

    public ReactivePaymentService(PaymentRepositoryPort repository,
                                  PaymentEventPublisherPort events,
                                  PaymentRiskPolicy riskPolicy,
                                  @Value("${payment.batch.max-concurrency:4}") int maxConcurrency) {
        this.repository = repository;
        this.events = events;
        this.riskPolicy = riskPolicy;
        this.maxConcurrency = maxConcurrency;
    }

    @Override
    @Transactional
    public Mono<Payment> createPayment(String merchantId, String customerId, BigDecimal amount, String currency) {
        return Mono.fromSupplier(() -> Payment.receive(merchantId, customerId, amount, currency))
                .flatMap(repository::save)
                .flatMap(payment -> events.publishPaymentChanged(payment).thenReturn(payment));
    }

    @Override
    @Transactional
    public Mono<Payment> authorizePayment(UUID paymentId) {
        return repository.findById(paymentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("payment not found: " + paymentId)))
                .flatMap(payment -> riskPolicy.isAllowed(payment)
                        .map(allowed -> allowed ? payment.authorize() : payment.reject()))
                .flatMap(repository::save)
                .flatMap(payment -> events.publishPaymentChanged(payment).thenReturn(payment));
    }

    @Override
    @Transactional
    public Mono<Payment> settlePayment(UUID paymentId) {
        return repository.findById(paymentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("payment not found: " + paymentId)))
                .map(Payment::settle)
                .flatMap(repository::save)
                .flatMap(payment -> events.publishPaymentChanged(payment).thenReturn(payment));
    }

    @Override
    public Mono<Payment> getPayment(UUID paymentId) {
        return repository.findById(paymentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("payment not found: " + paymentId)));
    }

    @Override
    public Flux<Payment> streamPaymentsByMerchant(String merchantId) {
        return repository.findByMerchantId(merchantId)
                .delayElements(Duration.ofMillis(150));
    }

    @Override
    public Flux<Payment> authorizeBatch(Flux<UUID> paymentIds) {
        return paymentIds
                .onBackpressureBuffer(100)
                .distinct()
                .flatMap(this::authorizePayment, maxConcurrency)
                .onErrorContinue((error, value) -> { });
    }
}
