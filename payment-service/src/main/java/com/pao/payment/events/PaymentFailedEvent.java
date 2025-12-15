package com.pao.payment.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {
    private String paymentId;
    private String orderId;
    private String reason;
    private LocalDateTime timestamp;
}
