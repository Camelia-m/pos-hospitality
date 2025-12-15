package com.pao.order.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private String id;

    private String tableId;
    private String serverId;
    private String terminalId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    private boolean synced;
    private LocalDateTime syncedAt;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    public void recalculateTotal() {
        subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        tax = subtotal.multiply(new BigDecimal("0.08"));
        total = subtotal.add(tax);
    }
}