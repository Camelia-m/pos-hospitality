package com.pao.order.controller;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class CreateOrderRequest {
    private String tableId;
    private String serverId;
    private String terminalId;
}