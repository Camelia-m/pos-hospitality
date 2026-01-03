### Getting Started: Your First Order Flow
**Purpose**: Learn the fundamentals by processing a complete order from creation to payment.

**What you'll learn**: How to set up the system, create an order, route it to the kitchen, and process payment.

**Prerequisites**:
- Java 21 installed
- Maven 3.8+ installed
- Docker and Docker Compose installed
- Basic understanding of REST APIs

**Step 1: Set Up Infrastructure**
```bash
# Clone the repository
git clone [repository-url]
cd pos-hospitality

# Start all infrastructure components
docker-compose up -d

# Verify everything is running
docker-compose ps
```

You should see Kafka, Zookeeper, PostgreSQL (3 instances), and Redis running.

**Step 2: Start the Services**
```bash
# Build all services
mvn clean install

# Terminal 1: Start Order Service
cd order-service
mvn spring-boot:run

# Terminal 2: Start Kitchen Service
cd kitchen-service
mvn spring-boot:run

# Terminal 3: Start Payment Service
cd payment-service
mvn spring-boot:run
```

Wait until you see "Started [ServiceName]Application" in each terminal.

**Step 3: Create Your First Order**
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": "TABLE-5",
    "serverId": "SERVER-123",
    "terminalId": "TERMINAL-1"
  }'
```

Save the `orderId` from the response - you'll need it for the next steps.

**Step 4: Add Items to the Order**
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
      }
    ]
  }'
```

**Step 5: Submit the Order**
```bash
curl -X POST http://localhost:8081/api/orders/{orderId}/submit
```

At this point, the Kitchen Service automatically receives the order via Kafka events.

**Step 6: Process in Kitchen**
Check the kitchen service logs - you'll see it received a ticket. Get the ticket ID and start preparation:
```bash
curl -X POST http://localhost:8082/api/kitchen/tickets/{ticketId}/start
```

Mark items ready:
```bash
curl -X POST http://localhost:8082/api/kitchen/tickets/{ticketId}/items/{itemId}/ready
```

**Step 7: Process Payment**
```bash
curl -X POST http://localhost:8083/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "{orderId}",
    "terminalId": "TERMINAL-1",
    "amount": 90.00,
    "tipAmount": 15.00,
    "method": "CREDIT_CARD",
    "idempotencyKey": "payment-123-unique"
  }'
```

**Expected outcome**: You've successfully created an order, routed it through the kitchen, and processed payment. Check the service logs to see the events flowing through Kafka.

**What you learned**: The complete order lifecycle, how services communicate via events, and the basic API structure.
