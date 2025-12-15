package com.pao.order.repository;

import com.pao.order.domain.Order;
import com.pao.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByTableId(String tableId);
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.synced = false ORDER BY o.createdAt")
    List<Order> findUnsyncedOrders();
}