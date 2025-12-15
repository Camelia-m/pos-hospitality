# Order at restaurant - Event-Driven Microservices Architecture

A production-ready, event-driven microservices architecture for hospitality Point-of-Sale systems, built with Spring Boot and designed for offline-first operation.

##  Architecture Overview

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Order     │────▶│   Kitchen   │────▶│   Payment   │
│  Service    │     │   Service   │     │   Service   │
└─────────────┘     └─────────────┘     └─────────────┘
       │                    │                    │
       └────────────────────┴────────────────────┘
                           │
                    ┌──────▼──────┐
                    │    Kafka    │
                    └─────────────┘
```

### Core Services

1. **Order Service** (Port 8081)
    - Order creation and management
    - Order item tracking with modifications
    - Event sourcing for full audit trail
    - Offline-friendly with sync queue

2. **Kitchen Service** (Port 8082)
    - Kitchen Display System (KDS)
    - Ticket management and routing
    - Station-based workflow
    - Course timing and preparation tracking

3. **Payment Service** (Port 8083)
    - Payment processing with idempotency
    - Split payment support
    - Offline payment queue with retry logic
    - Integration with payment gateways

## Key Features

### Event-Driven Architecture
- **Apache Kafka** for asynchronous event streaming
- **Event Sourcing** for order history and audit trails
- **CQRS Pattern** separating reads and writes
- **Saga Pattern** for distributed transactions

### Offline-First Design
- Local event log at each terminal
- Automatic sync when connectivity restored
- Exponential backoff retry strategy
- Conflict resolution with optimistic locking

### Data Integrity
- **Idempotency** for payment operations
- **Optimistic Locking** with JPA @Version
- **Transactional Outbox** pattern
- **Event deduplication** in consumers

### Scalability
- Horizontally scalable services
- Partitioned Kafka topics
- Database per service pattern
- Redis caching layer

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15
- Apache Kafka 3.5+

### Infrastructure Setup

```bash
# Start all infrastructure components
docker-compose up -d

# Verify services are running
docker-compose ps
```

This starts:
- Kafka (localhost:9092)
- Zookeeper (localhost:2181)
- PostgreSQL instances (ports 5432-5434)
- Redis (localhost:6379)

### Build & Run Services

```bash
# Build all services
mvn clean install

# Run Order Service
cd order-service
mvn spring-boot:run

# Run Kitchen Service (new terminal)
cd kitchen-service
mvn spring-boot:run

# Run Payment Service (new terminal)
cd payment-service
mvn spring-boot:run
```

## 📋 API Examples

### Create an Order

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": "TABLE-5",
    "serverId": "SERVER-123",
    "terminalId": "TERMINAL-1"
  }'
```

### Add Items to Order

```bash
curl -X POST http://localhost:8081/api/orders/{orderId}/items \
  -H "Content-Type: application/json" \
  -d '{
    "menuItemId": "ITEM-101",
    "name": "Ribeye Steak",
    "quantity": 2,
    "unitPrice": 45.00,
    "courseType": "MAIN",
    "modifications": [
      {
        "modificationId": "MOD-1",
        "name": "Medium Rare",
        "priceAdjustment": 0.00
      },
      {
        "modificationId": "MOD-2",
        "name": "Extra Sauce",
        "priceAdjustment": 2.50
      }
    ]
  }'
```

### Submit Order

```bash
curl -X POST http://localhost:8081/api/orders/{orderId}/submit
```

### Start Kitchen Ticket

```bash
curl -X POST http://localhost:8082/api/kitchen/tickets/{ticketId}/start
```

### Mark Item Ready

```bash
curl -X POST http://localhost:8082/api/kitchen/tickets/{ticketId}/items/{itemId}/ready
```

### Process Payment

```bash
curl -X POST http://localhost:8083/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "{orderId}",
    "terminalId": "TERMINAL-1",
    "amount": 95.00,
    "tipAmount": 15.00,
    "method": "CREDIT_CARD",
    "idempotencyKey": "unique-key-123"
  }'
```

## 🔄 Event Flow

### Order Placement Flow

```
1. Customer → Order Service: Create Order
2. Order Service → Kafka: OrderCreatedEvent
3. Server → Order Service: Add Items
4. Order Service → Kafka: OrderItemAddedEvent
5. Server → Order Service: Submit Order
6. Order Service → Kafka: OrderSubmittedEvent
7. Kitchen Service ← Kafka: Consumes OrderSubmittedEvent
8. Kitchen Service → Kitchen Display: Show Ticket
```

### Kitchen Preparation Flow

```
1. Cook → Kitchen Service: Start Ticket
2. Kitchen Service: Update status to IN_PROGRESS
3. Cook → Kitchen Service: Mark Items Ready
4. Kitchen Service → Kafka: ItemReadyEvent
5. When all items ready → Kafka: TicketCompletedEvent
6. Order Service ← Kafka: Updates order status
```

### Payment Flow

```
1. Server → Payment Service: Process Payment
2. Payment Service: Check idempotency
3. Payment Service → Payment Gateway: Authorize
4. If successful:
   - Payment Service → Kafka: PaymentProcessedEvent
   - Order Service ← Kafka: Mark order as PAID
5. If failed/offline:
   - Payment Service: Queue for retry
   - Background Job: Retry with exponential backoff
```

## 🛡️ Resilience Patterns

### Idempotency
```java
// Payment requests use idempotency keys
@Transactional
public Payment processPayment(..., String idempotencyKey) {
    Optional<Payment> existing = repository.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
        return existing.get(); // Return cached result
    }
    // Process new payment
}
```

### Offline Queue
```java
// Failed payments are queued
private void queueOfflinePayment(Payment payment) {
    OfflinePaymentQueue queueEntry = OfflinePaymentQueue.builder()
        .paymentId(payment.getId())
        .queuedAt(LocalDateTime.now())
        .retryCount(0)
        .nextRetryAt(LocalDateTime.now().plusMinutes(5))
        .build();
    queueRepository.save(queueEntry);
}

// Scheduled job processes queue
@Scheduled(fixedDelay = 60000)
public void processOfflinePayments() {
    List<OfflinePaymentQueue> pending = 
        queueRepository.findPendingPaymentsForRetry();
    // Process with exponential backoff
}
```

### Optimistic Locking
```java
@Entity
public class Order {
    @Version
    private Long version; // JPA handles concurrent updates
}
```

## 📊 Kafka Topics

| Topic | Partitions | Purpose |
|-------|------------|---------|
| `order-events` | 3 | Order lifecycle events |
| `kitchen-events` | 3 | Kitchen operations |
| `payment-events` | 3 | Payment processing |

### Event Schema Examples

**OrderSubmittedEvent**
```json
{
  "orderId": "uuid",
  "tableId": "TABLE-5",
  "total": 110.00,
  "items": [
    {
      "itemId": "uuid",
      "name": "Ribeye Steak",
      "quantity": 2,
      "courseType": "MAIN",
      "modifications": [...]
    }
  ],
  "timestamp": "2025-01-15T12:30:00"
}
```

**PaymentProcessedEvent**
```json
{
  "paymentId": "uuid",
  "orderId": "uuid",
  "amount": 95.00,
  "tipAmount": 15.00,
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "TXN-12345",
  "timestamp": "2025-01-15T12:35:00"
}
```

## 🔧 Configuration

### Environment Variables

```bash
# Order Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/order_service
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
REDIS_HOST=localhost

# Kitchen Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/kitchen_service
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Payment Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/payment_service
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Test with Kafka embedded
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order-events"})
class OrderServiceTest {
    // Tests here
}
```

## 📈 Monitoring & Observability

- **Logging**: SLF4J with Logback
- **Metrics**: Actuator endpoints
- **Health Checks**: `/actuator/health`
- **Kafka Lag Monitoring**: Consumer group offsets

## 🏆 Best Practices Implemented

1. **Domain-Driven Design**: Clear bounded contexts
2. **Database Per Service**: Data isolation
3. **API Versioning**: Future-proof endpoints
4. **Error Handling**: Comprehensive exception handling
5. **Validation**: Request validation with Bean Validation
6. **Documentation**: Swagger/OpenAPI ready
7. **Security**: Ready for Spring Security integration

## 🔮 Future Enhancements

- [ ] Menu Service with real-time availability
- [ ] Table Service with floor plan management
- [ ] Employee Service with permissions
- [ ] Inventory Service with stock tracking
- [ ] Analytics Service with reporting
- [ ] API Gateway with authentication
- [ ] Service Discovery with Eureka
- [ ] Distributed Tracing with Zipkin
- [ ] Circuit Breakers with Resilience4j

## 📝 License

MIT License - feel free to use for your projects!

## 🤝 Contributing

Contributions welcome! This is a reference architecture for learning and production use.

---

**Built with ❤️ for the hospitality industry**