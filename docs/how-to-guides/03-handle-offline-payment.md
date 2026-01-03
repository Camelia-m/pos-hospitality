### How to Handle Offline Payments

**When to use**: Payment gateway is unavailable and you need to queue payments for later processing.

**The system automatically handles this**, but here's how to monitor and troubleshoot:

**Steps**:

1. **Simulate offline scenario** by stopping the payment gateway or modifying the code to fail.

2. **Submit a payment** - it will be queued automatically:
```bash
curl -X POST http://localhost:8083/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order-123",
    "terminalId": "TERMINAL-1",
    "amount": 50.00,
    "method": "CREDIT_CARD",
    "idempotencyKey": "offline-payment-1"
  }'
```

3. **Check the offline queue** in the payment_service database:
```sql
SELECT * FROM offline_payment_queue 
WHERE status = 'PENDING' 
ORDER BY queued_at DESC;
```

4. **Monitor retry attempts** - the system retries automatically with exponential backoff:
    - First retry: 5 minutes
    - Second retry: 25 minutes
    - Third retry: 125 minutes

5. **Manually trigger retry** if needed:
```bash
# Restart the scheduled job by restarting the service
# Or directly call the retry endpoint if exposed
```

6. **Verify payment processing** after connectivity is restored - check that queued payments are processed and removed from the queue.

---
