package com.pao.payment.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment-events";

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent for payment: {}", event.getPaymentId());
        kafkaTemplate.send(TOPIC, event.getPaymentId(), event);
        kafkaTemplate.send("order-events", event.getOrderId(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent for payment: {}", event.getPaymentId());
        kafkaTemplate.send(TOPIC, event.getPaymentId(), event);
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        log.info("Publishing PaymentRefundedEvent for payment: {}", event.getPaymentId());
        kafkaTemplate.send(TOPIC, event.getPaymentId(), event);
    }
}