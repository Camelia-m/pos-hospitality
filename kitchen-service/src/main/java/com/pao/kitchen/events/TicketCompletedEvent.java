package com.pao.kitchen.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCompletedEvent {
    private String ticketId;
    private String orderId;
    private String tableId;
    private List<String> itemIds;
    private LocalDateTime timestamp;
}