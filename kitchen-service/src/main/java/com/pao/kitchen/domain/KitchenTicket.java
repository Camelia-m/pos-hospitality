package com.pao.kitchen.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kitchen_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenTicket {
    @Id
    private String id;

    private String orderId;
    private String tableId;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ticket", orphanRemoval = true)
    private List<TicketItem> items = new ArrayList<>();

    private String stationId;

    private LocalDateTime receivedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private Integer estimatedMinutes;

    public void addItem(TicketItem item) {
        items.add(item);
        item.setTicket(this);
    }
}