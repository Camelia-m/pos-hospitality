package com.pao.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "offline_payment_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflinePaymentQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String paymentId;
    private String orderId;

    @Column(columnDefinition = "TEXT")
    private String paymentData;

    private LocalDateTime queuedAt;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private LocalDateTime nextRetryAt;

    @Enumerated(EnumType.STRING)
    private QueueStatus status;
}
