package com.pao.kitchen.events;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ModificationDTO {
    private String modificationId;
    private String name;
    private BigDecimal priceAdjustment;
}