### How to Set Up CI/CD with GitHub Actions

**When to use**: You want automated testing and deployment on every commit.

**Steps**:

1. **Add required secrets** to your GitHub repository:
    - Settings → Secrets and variables → Actions
    - Add: `GCP_PROJECT_ID`, `GCP_SA_KEY`, `GCP_REGION`

2. **Create workflow file** `.github/workflows/deploy.yml`:
```yaml
name: Build and Deploy to Cloud Run

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: mvn test

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Cloud SDK
        uses: google-github-actions/setup-gcloud@v1
        with:
          service_account_key: ${{ secrets.GCP_SA_KEY }}
          project_id: ${{ secrets.GCP_PROJECT_ID }}
      
      - name: Build and Deploy Order Service
        run: |
          gcloud builds submit --tag gcr.io/${{ secrets.GCP_PROJECT_ID }}/order-service ./order-service
          gcloud run deploy order-service \
            --image gcr.io/${{ secrets.GCP_PROJECT_ID }}/order-service \
            --region ${{ secrets.GCP_REGION }} \
            --platform managed
```

3. **Push to GitHub** - the workflow runs automatically on push to main.

---