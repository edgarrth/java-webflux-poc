package com.example.payment.adapter.in.web;

import com.example.payment.domain.port.in.PaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/payments/v1/payments")
@Validated
public class PaymentController {
    private final PaymentUseCase useCase;

    public PaymentController(PaymentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public Mono<ResponseEntity<PaymentResponse>> create(@Valid @RequestBody Mono<CreatePaymentRequest> request) {
        return request
                .flatMap(req -> useCase.createPayment(req.merchantId(), req.customerId(), req.amount(), req.currency()))
                .map(PaymentResponse::from)
                .map(response -> ResponseEntity.created(URI.create("/payments/v1/payments/" + response.paymentId())).body(response));
    }

    @PostMapping("/{paymentId}/authorizations")
    public Mono<PaymentResponse> authorize(@PathVariable UUID paymentId) {
        return useCase.authorizePayment(paymentId).map(PaymentResponse::from);
    }

    @PostMapping("/{paymentId}/settlements")
    public Mono<PaymentResponse> settle(@PathVariable UUID paymentId) {
        return useCase.settlePayment(paymentId).map(PaymentResponse::from);
    }

    @GetMapping("/{paymentId}")
    public Mono<PaymentResponse> get(@PathVariable UUID paymentId) {
        return useCase.getPayment(paymentId).map(PaymentResponse::from);
    }

    @GetMapping(value = "/streams", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PaymentResponse> streamByMerchant(@RequestParam String merchantId) {
        return useCase.streamPaymentsByMerchant(merchantId).map(PaymentResponse::from);
    }

    @PostMapping(value = "/batch-authorizations", consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<PaymentResponse> authorizeBatch(@RequestBody Flux<BatchAuthorizeRequest> request) {
        return useCase.authorizeBatch(request.map(BatchAuthorizeRequest::paymentId)).map(PaymentResponse::from);
    }
}
