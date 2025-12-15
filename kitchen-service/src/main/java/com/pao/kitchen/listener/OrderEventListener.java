package com.pao.kitchen.listener;

import com.pao.kitchen.events.OrderSubmittedEvent;
import com.pao.kitchen.service.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final KitchenService kitchenService;

    @KafkaListener(topics = "kitchen-events", groupId = "kitchen-service")
    public void handleOrderSubmitted(OrderSubmittedEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderSubmittedEvent for order: {}", event.getOrderId());
            kitchenService.createTicketFromOrder(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderSubmittedEvent", e);
            // In production, implement retry logic or DLQ
        }
    }
}