package com.example.payment.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank String merchantId,
        @NotBlank String customerId,
        @DecimalMin("0.01") BigDecimal amount,
        @Pattern(regexp = "[A-Z]{3}") String currency
) { }
