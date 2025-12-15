package com.pao.payment.controller;

import com.pao.payment.domain.Payment;
import com.pao.payment.domain.PaymentMethod;
import com.pao.payment.service.PaymentService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> processPayment(@RequestBody ProcessPaymentRequest request) {
        String idempotencyKey = request.getIdempotencyKey() != null ?
                request.getIdempotencyKey() : UUID.randomUUID().toString();

        Payment payment = paymentService.processPayment(
                request.getOrderId(),
                request.getTerminalId(),
                request.getAmount(),
                request.getTipAmount(),
                request.getMethod(),
                idempotencyKey
        );

        return ResponseEntity.ok(payment);
    }
}

@Data
class ProcessPaymentRequest {
    private String orderId;
    private String terminalId;
    private BigDecimal amount;
    private BigDecimal tipAmount;
    private PaymentMethod method;
    private String idempotencyKey;
}
