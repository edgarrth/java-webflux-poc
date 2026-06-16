package com.example.payment.adapter.in.web;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, String code, String message) { }
