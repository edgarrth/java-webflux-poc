package com.example.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "spring.main.web-application-type=none")
class PaymentWebfluxApplicationContextTest {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Test
    void contextShouldProvideWebClientBuilder() {
        assertNotNull(webClientBuilder);
    }
}
