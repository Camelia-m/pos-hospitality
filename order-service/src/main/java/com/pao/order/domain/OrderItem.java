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
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String menuItemId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_item_id")
    private List<ItemModification> modifications = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private String courseType;
    private LocalDateTime sentToKitchenAt;

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        BigDecimal modPrice = modifications.stream()
                .map(ItemModification::getPriceAdjustment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalPrice = unitPrice.add(modPrice).multiply(new BigDecimal(quantity));
    }
}