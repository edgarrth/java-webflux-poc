package com.example.payment.application.service;

import com.example.payment.domain.model.Payment;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

public class PaymentRiskPolicy {
    public Mono<Boolean> isAllowed(Payment payment) {
        return Mono.just(payment.amount().compareTo(new BigDecimal("5000.00")) <= 0)
                .delayElement(Duration.ofMillis(80));
    }
}
