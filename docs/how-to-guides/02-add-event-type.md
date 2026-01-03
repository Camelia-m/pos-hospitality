### How to Add a New Event Type

**When to use**: You need to create a new event for inter-service communication.

**Example**: Adding a "TableAssignedEvent" when a server assigns themselves to a table.

**Steps**:

1. **Define the event class** in the appropriate service (order-service):
```java
// order-service/src/main/java/com/pos/order/event/TableAssignedEvent.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableAssignedEvent {
    private String orderId;
    private String tableId;
    private String serverId;
    private LocalDateTime timestamp;
}
```

2. **Publish the event** in the service layer:
```java
// order-service/src/main/java/com/pos/order/service/OrderService.java
private void publishTableAssignedEvent(Order order) {
    TableAssignedEvent event = TableAssignedEvent.builder()
        .orderId(order.getId())
        .tableId(order.getTableId())
        .serverId(order.getServerId())
        .timestamp(LocalDateTime.now())
        .build();
    
    kafkaTemplate.send("order-events", event);
}
```

3. **Create a consumer** in the receiving service if needed:
```java
// kitchen-service/src/main/java/com/pos/kitchen/consumer/OrderEventConsumer.java
@KafkaListener(topics = "order-events", groupId = "kitchen-service")
public void consumeTableAssignedEvent(TableAssignedEvent event) {
    log.info("Table {} assigned to server {} for order {}", 
        event.getTableId(), event.getServerId(), event.getOrderId());
    // Handle the event
}
```

4. **Test the event flow**:
    - Create an order with table assignment
    - Check Kitchen Service logs for the consumed event
    - Verify using ZooNavigator (http://localhost:9000)

---
