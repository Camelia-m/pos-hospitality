### System Architecture

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

### Service Registry

| Service | Port | Purpose | Database | Key Dependencies |
|---------|------|---------|----------|------------------|
| Order Service | 8081 | Order lifecycle management | order_service (5432) | Spring Boot, Kafka, Redis, PostgreSQL |
| Kitchen Service | 8082 | Kitchen Display System | kitchen_service (5433) | Spring Boot, Kafka, PostgreSQL |
| Payment Service | 8083 | Payment processing | payment_service (5434) | Spring Boot, Kafka, PostgreSQL |

### Infrastructure Components

| Component | Local Port | Purpose | Technology |
|-----------|-----------|---------|------------|
| Apache Kafka | 9092 | Event streaming | Confluent Platform 7.5+ |
| Zookeeper | 2181 | Kafka coordination | Confluent Platform |
| ZooNavigator | 9000 | Kafka UI | ZooNavigator |
| PostgreSQL (Orders) | 5432 | Order data | PostgreSQL 15 |
| PostgreSQL (Kitchen) | 5433 | Kitchen data | PostgreSQL 15 |
| PostgreSQL (Payment) | 5434 | Payment data | PostgreSQL 15 |
| Redis | 6379 | Caching layer | Redis latest |

### Kafka Topics

| Topic Name | Partitions | Retention | Producer | Consumers |
|------------|------------|-----------|----------|-----------|
| `order-events` | 3 | 7 days | Order Service | Kitchen Service, Payment Service |
| `kitchen-events` | 3 | 7 days | Kitchen Service | Order Service |
| `payment-events` | 3 | 7 days | Payment Service | Order Service |

### Event Schema Reference

#### OrderCreatedEvent
```json
{
  "orderId": "uuid",
  "tableId": "TABLE-5",
  "serverId": "SERVER-123",
  "terminalId": "TERMINAL-1",
  "status": "CREATED",
  "timestamp": "2025-01-15T12:30:00"
}
```

#### OrderItemAddedEvent
```json
{
  "orderId": "uuid",
  "itemId": "uuid",
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
    }
  ],
  "timestamp": "2025-01-15T12:30:30"
}
```

#### OrderSubmittedEvent
```json
{
  "orderId": "uuid",
  "tableId": "TABLE-5",
  "serverId": "SERVER-123",
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
  "timestamp": "2025-01-15T12:31:00"
}
```

#### KitchenTicketCreatedEvent
```json
{
  "ticketId": "uuid",
  "orderId": "uuid",
  "tableId": "TABLE-5",
  "items": [...],
  "priority": "NORMAL",
  "timestamp": "2025-01-15T12:31:05"
}
```

#### ItemReadyEvent
```json
{
  "ticketId": "uuid",
  "itemId": "uuid",
  "preparedBy": "COOK-456",
  "timestamp": "2025-01-15T12:45:00"
}
```

#### TicketCompletedEvent
```json
{
  "ticketId": "uuid",
  "orderId": "uuid",
  "completedAt": "2025-01-15T12:46:00"
}
```

#### PaymentProcessedEvent
```json
{
  "paymentId": "uuid",
  "orderId": "uuid",
  "amount": 95.00,
  "tipAmount": 15.00,
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "TXN-12345",
  "status": "COMPLETED",
  "timestamp": "2025-01-15T12:50:00"
}
```

### API Endpoints Reference

#### Order Service (Port 8081)

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/orders` | Create new order | `{ tableId, serverId, terminalId }` |
| GET | `/api/orders/{orderId}` | Get order details | - |
| POST | `/api/orders/{orderId}/items` | Add item to order | `{ menuItemId, name, quantity, unitPrice, courseType, modifications[] }` |
| PUT | `/api/orders/{orderId}/items/{itemId}` | Update order item | `{ quantity, modifications[] }` |
| DELETE | `/api/orders/{orderId}/items/{itemId}` | Remove item | - |
| POST | `/api/orders/{orderId}/submit` | Submit order to kitchen | - |
| GET | `/api/orders/table/{tableId}` | Get orders by table | - |

#### Kitchen Service (Port 8082)

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/api/kitchen/tickets` | Get all tickets | - |
| GET | `/api/kitchen/tickets/{ticketId}` | Get ticket details | - |
| POST | `/api/kitchen/tickets/{ticketId}/start` | Start ticket preparation | - |
| POST | `/api/kitchen/tickets/{ticketId}/items/{itemId}/ready` | Mark item ready | - |
| POST | `/api/kitchen/tickets/{ticketId}/complete` | Complete ticket | - |
| GET | `/api/kitchen/tickets/status/{status}` | Get tickets by status | - |

#### Payment Service (Port 8083)

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/payments` | Process payment | `{ orderId, terminalId, amount, tipAmount, method, idempotencyKey }` |
| GET | `/api/payments/{paymentId}` | Get payment details | - |
| GET | `/api/payments/order/{orderId}` | Get payments for order | - |
| POST | `/api/payments/{paymentId}/refund` | Refund payment | `{ amount, reason }` |

### Health Check Endpoints (All Services)

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | General health status |
| `/actuator/health/liveness` | Liveness probe (for K8s/Cloud Run) |
| `/actuator/health/readiness` | Readiness probe (for K8s/Cloud Run) |
| `/actuator/metrics` | Prometheus metrics |

### Environment Variables Reference

#### Order Service
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/order_service
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_CONSUMER_GROUP_ID=order-service
SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET=earliest

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Application
SERVER_PORT=8080
SPRING_APPLICATION_NAME=order-service
```

#### Kitchen Service
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/kitchen_service
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_CONSUMER_GROUP_ID=kitchen-service
SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET=earliest
SPRING_KAFKA_CONSUMER_ENABLE_AUTO_COMMIT=false

# Application
SERVER_PORT=8080
SPRING_APPLICATION_NAME=kitchen-service
```

#### Payment Service
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/payment_service
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_CONSUMER_GROUP_ID=payment-service
SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET=earliest

# Application
SERVER_PORT=8080
SPRING_APPLICATION_NAME=payment-service

# Payment Gateway (Configure based on provider)
PAYMENT_GATEWAY_URL=https://gateway.example.com
PAYMENT_GATEWAY_API_KEY=your-api-key
```

### Database Schema

#### Order Service Schema

**orders table**
```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    table_id VARCHAR(50) NOT NULL,
    server_id VARCHAR(50) NOT NULL,
    terminal_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(10,2),
    tax DECIMAL(10,2),
    total DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);
```

**order_items table**
```sql
CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    menu_item_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

**order_item_modifications table**
```sql
CREATE TABLE order_item_modifications (
    id UUID PRIMARY KEY,
    order_item_id UUID NOT NULL REFERENCES order_items(id),
    modification_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price_adjustment DECIMAL(10,2) DEFAULT 0.00
);
```

#### Kitchen Service Schema

**kitchen_tickets table**
```sql
CREATE TABLE kitchen_tickets (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    table_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);
```

**ticket_items table**
```sql
CREATE TABLE ticket_items (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES kitchen_tickets(id),
    order_item_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP,
    ready_at TIMESTAMP
);
```

#### Payment Service Schema

**payments table**
```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    terminal_id VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    tip_amount DECIMAL(10,2),
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
```

**offline_payment_queue table**
```sql
CREATE TABLE offline_payment_queue (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id),
    queued_at TIMESTAMP NOT NULL,
    retry_count INTEGER DEFAULT 0,
    next_retry_at TIMESTAMP NOT NULL,
    last_error TEXT,
    status VARCHAR(20) NOT NULL
);
```

### Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.2 |
| Event Streaming | Apache Kafka | 7.5+ (Confluent) |
| Database | PostgreSQL | 15 |
| Cache | Redis | Latest |
| API Documentation | SpringDoc OpenAPI | 2.x |
| Build Tool | Maven | 3.8+ |
| Container | Docker | Latest |
| Orchestration (Prod) | Google Cloud Run | N/A |
| CI/CD | GitHub Actions | N/A |

---