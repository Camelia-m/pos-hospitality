package com.pao.order.service;

import com.pao.order.domain.*;
import com.pao.order.events.*;
import com.pao.order.events.publisher.OrderEventPublisher;
import com.pao.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(String tableId, String serverId, String terminalId) {
        String orderId = UUID.randomUUID().toString();

        Order order = Order.builder()
                .id(orderId)
                .tableId(tableId)
                .serverId(serverId)
                .terminalId(terminalId)
                .status(OrderStatus.DRAFT)
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .synced(false)
                .build();

        order = orderRepository.save(order);

        eventPublisher.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(orderId)
                .tableId(tableId)
                .serverId(serverId)
                .terminalId(terminalId)
                .timestamp(LocalDateTime.now())
                .build());

        return order;
    }

    @Transactional
    public Order addItemToOrder(String orderId, String menuItemId, String name,
                                Integer quantity, BigDecimal unitPrice,
                                List<ItemModification> modifications, String courseType) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderItem item = OrderItem.builder()
                .menuItemId(menuItemId)
                .name(name)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .modifications(modifications != null ? modifications : new ArrayList<>())
                .status(ItemStatus.PENDING)
                .courseType(courseType)
                .build();

        order.addItem(item);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        List<ModificationDTO> modDTOs = modifications != null ?
                modifications.stream()
                        .map(m -> ModificationDTO.builder()
                                .modificationId(m.getModificationId())
                                .name(m.getName())
                                .priceAdjustment(m.getPriceAdjustment())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>();

        eventPublisher.publishOrderItemAdded(OrderItemAddedEvent.builder()
                .orderId(orderId)
                .itemId(item.getId())
                .menuItemId(menuItemId)
                .name(name)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .modifications(modDTOs)
                .timestamp(LocalDateTime.now())
                .build());

        return order;
    }

    @Transactional
    public Order submitOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.SUBMITTED);
        order.setUpdatedAt(LocalDateTime.now());

        order.getItems().forEach(item -> {
            item.setStatus(ItemStatus.SENT_TO_KITCHEN);
            item.setSentToKitchenAt(LocalDateTime.now());
        });

        order = orderRepository.save(order);

        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .itemId(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .courseType(item.getCourseType())
                        .modifications(item.getModifications().stream()
                                .map(m -> ModificationDTO.builder()
                                        .modificationId(m.getModificationId())
                                        .name(m.getName())
                                        .priceAdjustment(m.getPriceAdjustment())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        eventPublisher.publishOrderSubmitted(OrderSubmittedEvent.builder()
                .orderId(orderId)
                .tableId(order.getTableId())
                .total(order.getTotal())
                .items(itemDTOs)
                .timestamp(LocalDateTime.now())
                .build());

        return order;
    }

    public List<Order> getUnsyncedOrders() {
        return orderRepository.findUnsyncedOrders();
    }

    @Transactional
    public void markAsSynced(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setSynced(true);
        order.setSyncedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
