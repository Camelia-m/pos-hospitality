package com.pao.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MockPaymentGateway implements PaymentGateway {

    private final Map<String, PaymentGatewayResponse> processedPayments = new HashMap<>();

    @Override
    public PaymentGatewayResponse authorize(PaymentGatewayRequest request) {
        log.info("Processing payment with idempotency key: {}", request.getIdempotencyKey());

        // Idempotency check
        if (processedPayments.containsKey(request.getIdempotencyKey())) {
            log.info("Payment already processed, returning cached response");
            return processedPayments.get(request.getIdempotencyKey());
        }

        // Simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 95% success rate
        boolean success = Math.random() > 0.05;

        PaymentGatewayResponse response;
        if (success) {
            response = PaymentGatewayResponse.builder()
                    .success(true)
                    .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8))
                    .authorizationCode("AUTH-" + UUID.randomUUID().toString().substring(0, 6))
                    .build();
        } else {
            response = PaymentGatewayResponse.builder()
                    .success(false)
                    .errorMessage("Card declined")
                    .build();
        }

        processedPayments.put(request.getIdempotencyKey(), response);
        return response;
    }

    @Override
    public PaymentGatewayResponse capture(String transactionId) {
        return PaymentGatewayResponse.builder()
                .success(true)
                .transactionId(transactionId)
                .build();
    }

    @Override
    public PaymentGatewayResponse refund(String transactionId, BigDecimal amount) {
        return PaymentGatewayResponse.builder()
                .success(true)
                .transactionId("REFUND-" + transactionId)
                .build();
    }
}