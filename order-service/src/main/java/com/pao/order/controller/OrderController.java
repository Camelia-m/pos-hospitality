package com.pao.order.controller;

import com.pao.order.domain.Order;
import com.pao.order.service.OrderService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(
                request.getTableId(),
                request.getServerId(),
                request.getTerminalId()
        );
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<Order> addItem(@PathVariable String orderId,
                                         @RequestBody AddItemRequest request) {
        Order order = orderService.addItemToOrder(
                orderId,
                request.getMenuItemId(),
                request.getName(),
                request.getQuantity(),
                request.getUnitPrice(),
                request.getModifications(),
                request.getCourseType()
        );
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/submit")
    public ResponseEntity<Order> submitOrder(@PathVariable String orderId) {
        Order order = orderService.submitOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/unsynced")
    public ResponseEntity<List<Order>> getUnsyncedOrders() {
        return ResponseEntity.ok(orderService.getUnsyncedOrders());
    }
}

