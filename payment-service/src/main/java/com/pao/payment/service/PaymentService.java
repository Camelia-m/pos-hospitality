package com.pao.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pao.payment.domain.*;
import com.pao.payment.events.PaymentEventPublisher;
import com.pao.payment.events.PaymentFailedEvent;
import com.pao.payment.events.PaymentProcessedEvent;
import com.pao.payment.gateway.PaymentGateway;
import com.pao.payment.gateway.PaymentGatewayRequest;
import com.pao.payment.gateway.PaymentGatewayResponse;
import com.pao.payment.repository.OfflinePaymentQueueRepository;
import com.pao.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OfflinePaymentQueueRepository queueRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public Payment processPayment(String orderId, String terminalId,
                                  BigDecimal amount, BigDecimal tipAmount,
                                  PaymentMethod method, String idempotencyKey) {

        // Check idempotency
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Payment already processed with idempotency key: {}", idempotencyKey);
            return existing.get();
        }

        String paymentId = UUID.randomUUID().toString();
        BigDecimal totalAmount = amount.add(tipAmount);

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .terminalId(terminalId)
                .amount(amount)
                .tipAmount(tipAmount)
                .totalAmount(totalAmount)
                .method(method)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .synced(false)
                .retryCount(0)
                .idempotencyKey(idempotencyKey)
                .build();

        payment = paymentRepository.save(payment);

        // Attempt to process payment
        try {
            PaymentGatewayRequest request = PaymentGatewayRequest.builder()
                    .idempotencyKey(idempotencyKey)
                    .amount(totalAmount)
                    .paymentMethod(method.name())
                    .metadata(Map.of("orderId", orderId, "terminalId", terminalId))
                    .build();

            PaymentGatewayResponse response = paymentGateway.authorize(request);

            if (response.isSuccess()) {
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setTransactionId(response.getTransactionId());
                payment.setAuthorizationCode(response.getAuthorizationCode());
                payment.setProcessedAt(LocalDateTime.now());
                payment.setSynced(true);
                payment.setSyncedAt(LocalDateTime.now());

                payment = paymentRepository.save(payment);

                eventPublisher.publishPaymentProcessed(PaymentProcessedEvent.builder()
                        .paymentId(paymentId)
                        .orderId(orderId)
                        .amount(amount)
                        .tipAmount(tipAmount)
                        .paymentMethod(method.name())
                        .transactionId(response.getTransactionId())
                        .timestamp(LocalDateTime.now())
                        .build());

            } else {
                payment.setStatus(PaymentStatus.DECLINED);
                payment = paymentRepository.save(payment);

                eventPublisher.publishPaymentFailed(PaymentFailedEvent.builder()
                        .paymentId(paymentId)
                        .orderId(orderId)
                        .reason(response.getErrorMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
            }

        } catch (Exception e) {
            log.error("Payment processing failed, queueing for offline processing", e);
            payment.setStatus(PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);

            queueOfflinePayment(payment);
        }

        return payment;
    }

    private void queueOfflinePayment(Payment payment) {
        try {
            String paymentData = objectMapper.writeValueAsString(payment);

            OfflinePaymentQueue queueEntry = OfflinePaymentQueue.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .paymentData(paymentData)
                    .queuedAt(LocalDateTime.now())
                    .retryCount(0)
                    .nextRetryAt(LocalDateTime.now().plusMinutes(5))
                    .status(QueueStatus.PENDING)
                    .build();

            queueRepository.save(queueEntry);
            log.info("Payment queued for offline processing: {}", payment.getId());

        } catch (Exception e) {
            log.error("Failed to queue offline payment", e);
        }
    }

    @Transactional
    public void processOfflineQueue() {
        List<OfflinePaymentQueue> pendingPayments = queueRepository.findPendingPaymentsForRetry();

        log.info("Processing {} offline payments", pendingPayments.size());

        for (OfflinePaymentQueue queueEntry : pendingPayments) {
            try {
                Payment payment = paymentRepository.findById(queueEntry.getPaymentId())
                        .orElseThrow(() -> new RuntimeException("Payment not found"));

                if (payment.getStatus() == PaymentStatus.CAPTURED) {
                    queueEntry.setStatus(QueueStatus.COMPLETED);
                    queueRepository.save(queueEntry);
                    continue;
                }

                queueEntry.setStatus(QueueStatus.PROCESSING);
                queueEntry.setRetryCount(queueEntry.getRetryCount() + 1);
                queueEntry.setLastRetryAt(LocalDateTime.now());
                queueRepository.save(queueEntry);

                PaymentGatewayRequest request = PaymentGatewayRequest.builder()
                        .idempotencyKey(payment.getIdempotencyKey())
                        .amount(payment.getTotalAmount())
                        .paymentMethod(payment.getMethod().name())
                        .build();

                PaymentGatewayResponse response = paymentGateway.authorize(request);

                if (response.isSuccess()) {
                    payment.setStatus(PaymentStatus.CAPTURED);
                    payment.setTransactionId(response.getTransactionId());
                    payment.setAuthorizationCode(response.getAuthorizationCode());
                    payment.setProcessedAt(LocalDateTime.now());
                    payment.setSynced(true);
                    payment.setSyncedAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    queueEntry.setStatus(QueueStatus.COMPLETED);
                    queueRepository.save(queueEntry);

                    eventPublisher.publishPaymentProcessed(PaymentProcessedEvent.builder()
                            .paymentId(payment.getId())
                            .orderId(payment.getOrderId())
                            .amount(payment.getAmount())
                            .tipAmount(payment.getTipAmount())
                            .paymentMethod(payment.getMethod().name())
                            .transactionId(response.getTransactionId())
                            .timestamp(LocalDateTime.now())
                            .build());

                } else {
                    queueEntry.setStatus(QueueStatus.PENDING);
                    queueEntry.setNextRetryAt(calculateNextRetry(queueEntry.getRetryCount()));
                    queueRepository.save(queueEntry);
                }

            } catch (Exception e) {
                log.error("Error processing offline payment: {}", queueEntry.getPaymentId(), e);
                queueEntry.setStatus(QueueStatus.PENDING);
                queueEntry.setNextRetryAt(calculateNextRetry(queueEntry.getRetryCount()));
                queueRepository.save(queueEntry);
            }
        }
    }

    private LocalDateTime calculateNextRetry(int retryCount) {
        int[] backoffMinutes = {5, 15, 30, 60, 120};
        int index = Math.min(retryCount, backoffMinutes.length - 1);
        return LocalDateTime.now().plusMinutes(backoffMinutes[index]);
    }
}