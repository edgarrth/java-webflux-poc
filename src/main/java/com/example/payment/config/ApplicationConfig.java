package com.example.payment.config;

import com.example.payment.application.service.PaymentRiskPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    PaymentRiskPolicy paymentRiskPolicy() {
        return new PaymentRiskPolicy();
    }
}
