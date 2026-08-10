package com.example.payment;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentDomainTest {
    @Test
    void shouldAuthorizeReceivedPayment() {
        Payment payment = Payment.receive("merchant", "customer", new BigDecimal("100.00"), "PEN");
        assertEquals(PaymentStatus.AUTHORIZED, payment.authorize().status());
    }

    @Test
    void shouldRejectReceivedPayment() {
        Payment payment = Payment.receive("merchant", "customer", new BigDecimal("5100.00"), "PEN");
        assertEquals(PaymentStatus.REJECTED, payment.reject().status());
    }

    @Test
    void shouldSettleOnlyAuthorizedPayment() {
        Payment payment = Payment.receive("merchant", "customer", new BigDecimal("100.00"), "PEN");
        assertEquals(PaymentStatus.SETTLED, payment.authorize().settle().status());
        assertThrows(IllegalStateException.class, payment::settle);
    }

    @Test
    void shouldPreventInvalidAuthorizationTransition() {
        Payment payment = Payment.receive("merchant", "customer", new BigDecimal("100.00"), "PEN")
                .authorize()
                .settle();
        assertThrows(IllegalStateException.class, payment::authorize);
    }

    @Test
    void shouldValidateAmountAndCurrency() {
        assertThrows(IllegalArgumentException.class,
                () -> Payment.receive("merchant", "customer", BigDecimal.ZERO, "PEN"));
        assertThrows(IllegalArgumentException.class,
                () -> Payment.receive("merchant", "customer", new BigDecimal("1.00"), "pen"));
    }
}
