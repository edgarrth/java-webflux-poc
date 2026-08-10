package com.example.payment;

import com.example.payment.application.service.PaymentRiskPolicy;
import com.example.payment.application.usecase.PaymentNotFoundException;
import com.example.payment.application.usecase.ReactivePaymentService;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.model.PaymentStatus;
import com.example.payment.domain.port.out.PaymentEventPublisherPort;
import com.example.payment.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class ReactivePaymentServiceTest {

    @Test
    void shouldAuthorizePaymentWhenRiskPolicyAllowsIt() {
        InMemoryRepository repository = new InMemoryRepository();
        Payment payment = Payment.receive("merchant", "customer", new BigDecimal("100.00"), "PEN");
        repository.put(payment);
        ReactivePaymentService service = service(repository);

        StepVerifier.create(service.authorizePayment(payment.paymentId()))
                .expectNextMatches(result -> result.status() == PaymentStatus.AUTHORIZED)
                .verifyComplete();
    }

    @Test
    void shouldRejectPaymentAboveRiskLimit() {
        InMemoryRepository repository = new InMemoryRepository();
        Payment payment = Payment.receive("merchant", "customer", new BigDecimal("5000.01"), "PEN");
        repository.put(payment);
        ReactivePaymentService service = service(repository);

        StepVerifier.create(service.authorizePayment(payment.paymentId()))
                .expectNextMatches(result -> result.status() == PaymentStatus.REJECTED)
                .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundForUnknownPayment() {
        ReactivePaymentService service = service(new InMemoryRepository());

        StepVerifier.create(service.getPayment(UUID.randomUUID()))
                .expectError(PaymentNotFoundException.class)
                .verify();
    }

    @Test
    void batchShouldSkipInvalidIdsAndContinue() {
        InMemoryRepository repository = new InMemoryRepository();
        Payment valid = Payment.receive("merchant", "customer", new BigDecimal("100.00"), "PEN");
        repository.put(valid);
        ReactivePaymentService service = service(repository);

        StepVerifier.create(service.authorizeBatch(Flux.just(UUID.randomUUID(), valid.paymentId())))
                .expectNextMatches(result -> result.paymentId().equals(valid.paymentId())
                        && result.status() == PaymentStatus.AUTHORIZED)
                .verifyComplete();
    }

    private ReactivePaymentService service(InMemoryRepository repository) {
        PaymentEventPublisherPort events = payment -> Mono.empty();
        return new ReactivePaymentService(repository, events, new PaymentRiskPolicy(), 4, 100);
    }

    static final class InMemoryRepository implements PaymentRepositoryPort {
        private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

        void put(Payment payment) {
            payments.put(payment.paymentId(), payment);
        }

        @Override
        public Mono<Payment> save(Payment payment) {
            put(payment);
            return Mono.just(payment);
        }

        @Override
        public Mono<Payment> findById(UUID paymentId) {
            return Mono.justOrEmpty(payments.get(paymentId));
        }

        @Override
        public Flux<Payment> findByMerchantId(String merchantId) {
            return Flux.fromIterable(payments.values())
                    .filter(payment -> payment.merchantId().equals(merchantId));
        }
    }
}
