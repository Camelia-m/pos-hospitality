
### How to Deploy to Google Cloud Run

**When to use**: You need to deploy services to production in Google Cloud.

**Prerequisites**:
- Google Cloud SDK installed and authenticated
- GCP project with Cloud Run API enabled
- Services tested locally

**Steps**:

1. **Build and push Docker images to GCR**
```bash
# Set your project ID
export PROJECT_ID=your-gcp-project-id

# Build and push Order Service
gcloud builds submit --tag gcr.io/$PROJECT_ID/order-service ./order-service

# Build and push Kitchen Service
gcloud builds submit --tag gcr.io/$PROJECT_ID/kitchen-service ./kitchen-service

# Build and push Payment Service
gcloud builds submit --tag gcr.io/$PROJECT_ID/payment-service ./payment-service
```

2. **Set up Cloud SQL (PostgreSQL)**
```bash
gcloud sql instances create pos-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1
```

3. **Create databases for each service**
```bash
gcloud sql databases create order_service --instance=pos-db
gcloud sql databases create kitchen_service --instance=pos-db
gcloud sql databases create payment_service --instance=pos-db
```

4. **Deploy Order Service**
```bash
gcloud run deploy order-service \
  --image gcr.io/$PROJECT_ID/order-service \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 1 \
  --max-instances 10 \
  --set-env-vars="SPRING_DATASOURCE_URL=jdbc:postgresql://CLOUD_SQL_HOST/order_service,SPRING_KAFKA_BOOTSTRAP_SERVERS=KAFKA_HOST:9092"
```

5. **Repeat for Kitchen and Payment Services** (adjust database names and ports)

6. **Verify deployment**
```bash
gcloud run services list
```

**Result**: Services are deployed and auto-scaling on Cloud Run.

---
