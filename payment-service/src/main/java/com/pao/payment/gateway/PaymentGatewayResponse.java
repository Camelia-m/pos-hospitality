package com.pao.payment.gateway;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayResponse {
    private boolean success;
    private String transactionId;
    private String authorizationCode;
    private String errorMessage;
}