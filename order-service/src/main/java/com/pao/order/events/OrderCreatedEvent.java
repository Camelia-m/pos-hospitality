package com.pao.order.events;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderCreatedEvent {
    private String orderId;
    private String tableId;
    private String serverId;
    private String terminalId;
    private LocalDateTime timestamp;
}