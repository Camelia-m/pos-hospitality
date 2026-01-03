### Building Your Development Environment
**Purpose**: Set up a local development environment for modifying and extending services.

**Prerequisites**:
- Completed "Your First Order Flow" tutorial
- IDE with Java support (IntelliJ IDEA, Eclipse, or VS Code)

**Step 1: Import Projects**
Import each service (order-service, kitchen-service, payment-service) as separate Maven projects in your IDE.

**Step 2: Configure Database Connections**
Each service uses its own PostgreSQL database. The docker-compose setup creates three databases on different ports:
- order_service: localhost:5432
- kitchen_service: localhost:5433
- payment_service: localhost:5434

**Step 3: Explore the Code Structure**
Each service follows the same package structure:
```
src/main/java/com/pos/[service]/
  ├── controller/    # REST API endpoints
  ├── service/       # Business logic
  ├── repository/    # Data access
  ├── model/         # Domain entities
  ├── event/         # Kafka event definitions
  └── config/        # Spring configuration
```

**Step 4: Make Your First Change**
Add a new field to the Order entity:
1. Open `order-service/src/main/java/com/pos/order/model/Order.java`
2. Add a new field: `private String customerName;`
3. Add getter/setter methods
4. Restart the Order Service
5. Test with your modified API call

**Step 5: Run Tests**
```bash
# Run unit tests for a service
cd order-service
mvn test

# Run integration tests
mvn verify
```

**Expected outcome**: A working development environment where you can modify services and see changes immediately.

---
