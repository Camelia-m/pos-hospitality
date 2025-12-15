package com.pao.kitchen.repository;

import com.pao.kitchen.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, String> {
    List<KitchenTicket> findByStatusIn(List<TicketStatus> statuses);
    List<KitchenTicket> findByStationIdAndStatusIn(String stationId, List<TicketStatus> statuses);

    @Query("SELECT t FROM KitchenTicket t WHERE t.status IN ('NEW', 'IN_PROGRESS') ORDER BY t.priority DESC, t.receivedAt ASC")
    List<KitchenTicket> findActiveTicketsOrdered();
}