package com.pao.kitchen.events;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderItemDTO {
    private String itemId;
    private String menuItemId;
    private String name;
    private Integer quantity;
    private String courseType;
    private List<ModificationDTO> modifications;
}