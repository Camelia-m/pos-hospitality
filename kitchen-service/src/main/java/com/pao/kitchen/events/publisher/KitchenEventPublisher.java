package com.pao.kitchen.events.publisher;

import com.pao.kitchen.events.ItemReadyEvent;
import com.pao.kitchen.events.TicketCompletedEvent;
import com.pao.kitchen.events.TicketCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTicketCreated(TicketCreatedEvent event) {
        log.info("Publishing TicketCreatedEvent for ticket: {}", event.getTicketId());
        kafkaTemplate.send("kitchen-events", event.getTicketId(), event);
    }

    public void publishItemReady(ItemReadyEvent event) {
        log.info("Publishing ItemReadyEvent for item: {}", event.getItemId());
        kafkaTemplate.send("kitchen-events", event.getTicketId(), event);
    }

    public void publishTicketCompleted(TicketCompletedEvent event) {
        log.info("Publishing TicketCompletedEvent for ticket: {}", event.getTicketId());
        kafkaTemplate.send("kitchen-events", event.getTicketId(), event);
        kafkaTemplate.send("order-events", event.getOrderId(), event);
    }
}