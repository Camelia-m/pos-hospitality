package com.pao.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    private String id;

    private String orderId;
    private String terminalId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private BigDecimal amount;
    private BigDecimal tipAmount;
    private BigDecimal totalAmount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "payment", orphanRemoval = true)
    private List<PaymentSplit> splits = new ArrayList<>();

    private String transactionId;
    private String authorizationCode;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    // Offline support
    private boolean synced;
    private LocalDateTime syncedAt;
    private Integer retryCount;

    // Idempotency
    private String idempotencyKey;

    @Version
    private Long version;

    public void addSplit(PaymentSplit split) {
        splits.add(split);
        split.setPayment(this);
    }
}
