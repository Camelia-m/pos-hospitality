package com.pao.payment.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefundedEvent {
    private String paymentId;
    private String orderId;
    private BigDecimal refundAmount;
    private String reason;
    private LocalDateTime timestamp;
}

