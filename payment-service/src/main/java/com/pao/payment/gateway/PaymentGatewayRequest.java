package com.pao.payment.gateway;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class PaymentGatewayRequest {
    private String idempotencyKey;
    private BigDecimal amount;
    private String paymentMethod;
    private String cardToken;
    private Map<String, String> metadata;
}