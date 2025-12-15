package com.pao.payment.repository;

import com.pao.payment.domain.OfflinePaymentQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfflinePaymentQueueRepository extends JpaRepository<OfflinePaymentQueue, String> {
    @Query("SELECT q FROM OfflinePaymentQueue q WHERE q.status = 'PENDING' " +
            "AND q.nextRetryAt <= CURRENT_TIMESTAMP ORDER BY q.queuedAt")
    List<OfflinePaymentQueue> findPendingPaymentsForRetry();
}