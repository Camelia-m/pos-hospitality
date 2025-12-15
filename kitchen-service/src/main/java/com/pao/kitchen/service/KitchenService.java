package com.pao.kitchen.service;

import com.pao.kitchen.domain.*;
import com.pao.kitchen.events.*;
import com.pao.kitchen.events.publisher.KitchenEventPublisher;
import com.pao.kitchen.repository.KitchenTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenService {

    private final KitchenTicketRepository ticketRepository;
    private final KitchenEventPublisher eventPublisher;

    @Transactional
    public KitchenTicket createTicketFromOrder(OrderSubmittedEvent event) {
        String ticketId = UUID.randomUUID().toString();
        String stationId = determineStation(event.getItems());

        KitchenTicket ticket = KitchenTicket.builder()
                .id(ticketId)
                .orderId(event.getOrderId())
                .tableId(event.getTableId())
                .status(TicketStatus.NEW)
                .priority(TicketPriority.NORMAL)
                .stationId(stationId)
                .receivedAt(LocalDateTime.now())
                .estimatedMinutes(calculateEstimatedTime(event.getItems()))
                .build();

        // Create a final reference for use in lambda
        final KitchenTicket finalTicket = ticket;

        event.getItems().forEach(item -> {
            List<String> mods = item.getModifications().stream()
                    .map(ModificationDTO::getName)
                    .collect(Collectors.toList());

            TicketItem ticketItem = TicketItem.builder()
                    .orderItemId(item.getItemId())
                    .itemName(item.getName())
                    .quantity(item.getQuantity())
                    .modifications(mods)
                    .status(ItemStatus.PENDING)
                    .courseType(item.getCourseType())
                    .build();

            finalTicket.addItem(ticketItem);
        });

        ticket = ticketRepository.save(ticket);

        eventPublisher.publishTicketCreated(TicketCreatedEvent.builder()
                .ticketId(ticketId)
                .orderId(event.getOrderId())
                .tableId(event.getTableId())
                .stationId(stationId)
                .timestamp(LocalDateTime.now())
                .build());

        return ticket;
    }

    @Transactional
    public KitchenTicket startTicket(String ticketId) {
        KitchenTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setStartedAt(LocalDateTime.now());

        ticket.getItems().forEach(item -> {
            item.setStatus(ItemStatus.PREPARING);
            item.setStartedAt(LocalDateTime.now());
        });

        return ticketRepository.save(ticket);
    }

    @Transactional
    public KitchenTicket markItemReady(String ticketId, String itemId) {
        KitchenTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketItem item = ticket.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setStatus(ItemStatus.READY);
        item.setCompletedAt(LocalDateTime.now());

        eventPublisher.publishItemReady(ItemReadyEvent.builder()
                .ticketId(ticketId)
                .orderId(ticket.getOrderId())
                .itemId(itemId)
                .itemName(item.getItemName())
                .timestamp(LocalDateTime.now())
                .build());

        boolean allReady = ticket.getItems().stream()
                .allMatch(i -> i.getStatus() == ItemStatus.READY);

        if (allReady) {
            ticket.setStatus(TicketStatus.READY);
            ticket.setCompletedAt(LocalDateTime.now());

            List<String> itemIds = ticket.getItems().stream()
                    .map(TicketItem::getOrderItemId)
                    .collect(Collectors.toList());

            eventPublisher.publishTicketCompleted(TicketCompletedEvent.builder()
                    .ticketId(ticketId)
                    .orderId(ticket.getOrderId())
                    .tableId(ticket.getTableId())
                    .itemIds(itemIds)
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        return ticketRepository.save(ticket);
    }

    public List<KitchenTicket> getActiveTickets() {
        return ticketRepository.findActiveTicketsOrdered();
    }

    private String determineStation(List<OrderItemDTO> items) {
        // Simple logic - in reality, this would query menu service
        boolean hasGrill = items.stream()
                .anyMatch(i -> i.getName().toLowerCase().contains("steak") ||
                        i.getName().toLowerCase().contains("burger"));

        return hasGrill ? "GRILL_STATION" : "HOT_STATION";
    }

    private Integer calculateEstimatedTime(List<OrderItemDTO> items) {
        return items.size() * 5 + 10; // Simple calculation
    }
}