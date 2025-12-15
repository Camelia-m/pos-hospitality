package com.pao.kitchen.events;

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
public class OrderSubmittedEvent {
    private String orderId;
    private String tableId;
    private BigDecimal total;
    private List<OrderItemDTO> items;
    private LocalDateTime timestamp;
}
