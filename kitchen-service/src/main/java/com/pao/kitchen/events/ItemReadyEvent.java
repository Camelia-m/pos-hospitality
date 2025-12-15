package com.pao.kitchen.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemReadyEvent {
    private String ticketId;
    private String orderId;
    private String itemId;
    private String itemName;
    private LocalDateTime timestamp;
}