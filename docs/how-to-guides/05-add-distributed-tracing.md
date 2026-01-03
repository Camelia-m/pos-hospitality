### How to Add Distributed Tracing

**When to use**: You need to trace requests across multiple services for debugging.

**Steps**:

1. **Add dependencies** to each service's pom.xml:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

2. **Configure Zipkin** in application.yml:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

3. **Start Zipkin** via Docker:
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

4. **Access Zipkin UI** at http://localhost:9411 to view traces across services.

---
