package com.pao.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_splits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSplit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String customerId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String transactionId;
}