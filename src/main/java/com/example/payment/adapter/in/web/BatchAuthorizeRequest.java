package com.example.payment.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BatchAuthorizeRequest(@NotNull UUID paymentId) { }
