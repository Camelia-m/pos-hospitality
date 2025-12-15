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
@Table(name = "ticket_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private KitchenTicket ticket;

    private String orderItemId;
    private String itemName;
    private Integer quantity;

    @ElementCollection
    @CollectionTable(name = "ticket_item_modifications")
    private List<String> modifications = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private String courseType;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}