### How to Scale a Service Horizontally

**When to use**: A service is experiencing high load and needs more instances.

**For Cloud Run (Production)**:

```bash
# Update max instances
gcloud run services update order-service \
  --max-instances 20 \
  --region us-central1

# Update concurrency per instance
gcloud run services update order-service \
  --concurrency 80 \
  --region us-central1
```

**For Local Docker Deployment**:

```bash
# Run multiple instances with load balancer
docker run -d -p 8081:8080 --name order-service-1 order-service:latest
docker run -d -p 8091:8080 --name order-service-2 order-service:latest
docker run -d -p 8092:8080 --name order-service-3 order-service:latest

# Configure nginx as load balancer
# (nginx.conf configuration needed)
```

**Considerations**:
- Kafka consumer groups automatically distribute partitions across instances
- Database connections should be pooled appropriately
- Redis cache is shared across all instances
- Ensure session state is not stored in-memory

---
