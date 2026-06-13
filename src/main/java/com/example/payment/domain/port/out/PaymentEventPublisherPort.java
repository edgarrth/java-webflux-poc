package com.example.payment.domain.port.out;

import com.example.payment.domain.model.Payment;
import reactor.core.publisher.Mono;

public interface PaymentEventPublisherPort {
    Mono<Void> publishPaymentChanged(Payment payment);
}
