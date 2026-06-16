package com.example.payment.adapter.in.web;

public record StreamingDemoResponse(
        String step,
        String sourceEndpoint,
        long configuredDelayMs,
        long elapsedMs,
        String message
) {
}
