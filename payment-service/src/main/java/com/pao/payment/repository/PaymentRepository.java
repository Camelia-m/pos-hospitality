package com.pao.payment.repository;

import com.pao.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByOrderId(String orderId);

    @Query("SELECT p FROM Payment p WHERE p.synced = false ORDER BY p.createdAt")
    List<Payment> findUnsyncedPayments();
}

