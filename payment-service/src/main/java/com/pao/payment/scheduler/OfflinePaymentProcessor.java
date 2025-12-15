package com.pao.payment.scheduler;

import com.pao.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OfflinePaymentProcessor {

    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 60000) // Every minute
    public void processOfflinePayments() {
        log.debug("Running offline payment processor");
        paymentService.processOfflineQueue();
    }
}