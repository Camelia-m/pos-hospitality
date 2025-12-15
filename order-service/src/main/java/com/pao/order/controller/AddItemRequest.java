package com.pao.order.controller;

import com.pao.order.domain.ItemModification;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
public class AddItemRequest {
    private String menuItemId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
    private List<ItemModification> modifications;
    private String courseType;
}