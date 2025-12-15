package com.pao.payment.events;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent {
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private BigDecimal tipAmount;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime timestamp;
}