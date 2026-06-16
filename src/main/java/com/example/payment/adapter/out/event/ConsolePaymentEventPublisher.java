package com.example.payment.adapter.out.event;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.port.out.PaymentEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ConsolePaymentEventPublisher implements PaymentEventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(ConsolePaymentEventPublisher.class);

    @Override
    public Mono<Void> publishPaymentChanged(Payment payment) {
        return Mono.fromRunnable(() -> log.info("payment.changed id={} status={} amount={}", payment.paymentId(), payment.status(), payment.amount()))
                .then();
    }
}
