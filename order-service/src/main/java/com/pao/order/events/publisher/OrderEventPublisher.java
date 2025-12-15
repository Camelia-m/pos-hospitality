package com.pao.order.events.publisher;

import com.pao.order.events.OrderCreatedEvent;
import com.pao.order.events.OrderItemAddedEvent;
import com.pao.order.events.OrderSubmittedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    private static final String TOPIC = "order-events";

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for order: {}", event.getOrderId());
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
    }

    public void publishOrderItemAdded(OrderItemAddedEvent event) {
        log.info("Publishing OrderItemAddedEvent for order: {}", event.getOrderId());
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
    }

    public void publishOrderSubmitted(OrderSubmittedEvent event) {
        log.info("Publishing OrderSubmittedEvent for order: {}", event.getOrderId());
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
        kafkaTemplate.send("kitchen-events", event.getOrderId(), event);
    }
}