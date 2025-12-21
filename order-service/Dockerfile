FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy the entire project structure
COPY . .

# First, install the parent POM
RUN mvn -N install

# Then build order-service specifically with Spring Boot plugin
RUN cd order-service && mvn clean package spring-boot:repackage -DskipTests

# Debug: List what was created
RUN ls -la /build/order-service/target/

# Debug: Check the JAR manifest
RUN cd /build/order-service/target && \
    for jar in *.jar; do \
        echo "Checking $jar:"; \
        unzip -q -c "$jar" META-INF/MANIFEST.MF || echo "No manifest in $jar"; \
    done

FROM eclipse-temurin:21-jre

WORKDIR /app
ENV PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Copy the executable jar
COPY --from=build /build/order-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]