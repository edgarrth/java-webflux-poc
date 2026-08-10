package com.example.payment.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/payments/v1/streaming-demos")
public class StreamingDemoController {

    private final WebClient webClient;

    public StreamingDemoController(WebClient.Builder webClientBuilder,
                                   @Value("${server.port:8080}") int serverPort) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:" + serverPort)
                .build();
    }

    @GetMapping(value = "/orchestrations/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamingDemoResponse>> orchestrateWithSse() {
        Instant startedAt = Instant.now();

        return Flux.merge(callBackendEndpoint("/payments/v1/streaming-demos/backends/customer-profile", startedAt),
                        callBackendEndpoint("/payments/v1/streaming-demos/backends/risk-score", startedAt),
                        callBackendEndpoint("/payments/v1/streaming-demos/backends/fraud-validation", startedAt),
                        callBackendEndpoint("/payments/v1/streaming-demos/backends/loyalty-benefits", startedAt))
                .map(response -> ServerSentEvent.<StreamingDemoResponse>builder()
                        .event("demo-step")
                        .id(response.step())
                        .data(response)
                        .build())
                .concatWith(Mono.defer(() -> Mono.just(
                        ServerSentEvent.<StreamingDemoResponse>builder()
                                .event("completed")
                                .data(new StreamingDemoResponse(
                                        "completed",
                                        "/payments/v1/streaming-demos/orchestrations/sse",
                                        0,
                                        Duration.between(startedAt, Instant.now()).toMillis(),
                                        "SSE orchestration finished"
                                ))
                                .build())));
    }

    @GetMapping(value = "/orchestrations/ndjson", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<StreamingDemoResponse> orchestrateWithNdjson() {
        Instant startedAt = Instant.now();

        return Flux.merge(callBackendEndpoint("/payments/v1/streaming-demos/backends/customer-profile", startedAt),
                        callBackendEndpoint("/payments/v1/streaming-demos/backends/risk-score", startedAt),
                        callBackendEndpoint("/payments/v1/streaming-demos/backends/fraud-validation", startedAt),
                        callBackendEndpoint("/payments/v1/streaming-demos/backends/loyalty-benefits", startedAt))
                .concatWith(Mono.defer(() -> Mono.just(new StreamingDemoResponse(
                        "completed",
                        "/payments/v1/streaming-demos/orchestrations/ndjson",
                        0,
                        Duration.between(startedAt, Instant.now()).toMillis(),
                        "NDJSON orchestration finished"
                ))));
    }

    @GetMapping("/backends/customer-profile")
    public Mono<StreamingDemoResponse> customerProfile() {
        return simulateBackend("customer-profile", "/payments/v1/streaming-demos/backends/customer-profile", 1_000,
                "Customer profile loaded");
    }

    @GetMapping("/backends/risk-score")
    public Mono<StreamingDemoResponse> riskScore() {
        return simulateBackend("risk-score", "/payments/v1/streaming-demos/backends/risk-score", 2_500,
                "Risk score calculated");
    }

    @GetMapping("/backends/fraud-validation")
    public Mono<StreamingDemoResponse> fraudValidation() {
        return simulateBackend("fraud-validation", "/payments/v1/streaming-demos/backends/fraud-validation", 4_000,
                "Fraud validation completed");
    }

    @GetMapping("/backends/loyalty-benefits")
    public Mono<StreamingDemoResponse> loyaltyBenefits() {
        return simulateBackend("loyalty-benefits", "/payments/v1/streaming-demos/backends/loyalty-benefits", 6_000,
                "Loyalty benefits loaded");
    }

    @GetMapping("/backends")
    public List<String> backendEndpoints() {
        return List.of(
                "/payments/v1/streaming-demos/backends/customer-profile",
                "/payments/v1/streaming-demos/backends/risk-score",
                "/payments/v1/streaming-demos/backends/fraud-validation",
                "/payments/v1/streaming-demos/backends/loyalty-benefits"
        );
    }

    private Mono<StreamingDemoResponse> callBackendEndpoint(String path, Instant orchestrationStartedAt) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(StreamingDemoResponse.class)
                .map(response -> new StreamingDemoResponse(
                        response.step(),
                        response.sourceEndpoint(),
                        response.configuredDelayMs(),
                        Duration.between(orchestrationStartedAt, Instant.now()).toMillis(),
                        response.message()
                ));
    }

    private Mono<StreamingDemoResponse> simulateBackend(String step, String sourceEndpoint, long delayMs, String message) {
        Instant startedAt = Instant.now();

        return Mono.delay(Duration.ofMillis(delayMs))
                .map(ignored -> new StreamingDemoResponse(
                        step,
                        sourceEndpoint,
                        delayMs,
                        Duration.between(startedAt, Instant.now()).toMillis(),
                        message
                ));
    }
}
