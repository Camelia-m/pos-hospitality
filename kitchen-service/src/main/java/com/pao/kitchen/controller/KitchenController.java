package com.pao.kitchen.controller;

import com.pao.kitchen.domain.KitchenTicket;
import com.pao.kitchen.service.KitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/kitchen/tickets")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping("/active")
    public ResponseEntity<List<KitchenTicket>> getActiveTickets() {
        return ResponseEntity.ok(kitchenService.getActiveTickets());
    }

    @PostMapping("/{ticketId}/start")
    public ResponseEntity<KitchenTicket> startTicket(@PathVariable String ticketId) {
        KitchenTicket ticket = kitchenService.startTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{ticketId}/items/{itemId}/ready")
    public ResponseEntity<KitchenTicket> markItemReady(
            @PathVariable String ticketId,
            @PathVariable String itemId) {
        KitchenTicket ticket = kitchenService.markItemReady(ticketId, itemId);
        return ResponseEntity.ok(ticket);
    }
}
