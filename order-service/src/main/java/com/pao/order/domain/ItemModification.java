package com.pao.order.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "item_modifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemModification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String modificationId;
    private String name;
    private BigDecimal priceAdjustment;
    private String specialInstructions;
}
