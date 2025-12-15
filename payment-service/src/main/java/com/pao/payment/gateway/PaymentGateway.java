package com.pao.payment.gateway;

import lombok.*;
import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentGatewayResponse authorize(PaymentGatewayRequest request);
    PaymentGatewayResponse capture(String transactionId);
    PaymentGatewayResponse refund(String transactionId, BigDecimal amount);
}