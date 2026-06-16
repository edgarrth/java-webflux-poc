package com.example.payment;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentDomainTest {
    @Test
    void shouldAuthorizePayment() {
        Payment payment = Payment.receive("merchant", "customer", new BigDecimal("100.00"), "PEN");
        assertEquals(PaymentStatus.AUTHORIZED, payment.authorize().status());
    }
}
