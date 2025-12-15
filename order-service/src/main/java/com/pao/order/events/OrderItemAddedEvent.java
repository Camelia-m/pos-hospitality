package com.pao.order.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemAddedEvent {
    private String orderId;
    private String itemId;
    private String menuItemId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
    private List<ModificationDTO> modifications;
    private LocalDateTime timestamp;
}
