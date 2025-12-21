FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy parent POM first
COPY pom.xml .

# Copy payment-service module
COPY payment-service ./payment-service

# Build the service - the spring-boot-maven-plugin should repackage automatically
RUN mvn -pl payment-service -am clean install -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
ENV PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Copy the executable jar (should be in target/)
COPY --from=build /build/payment-service/target/*.jar app.jar

# Verify the jar is executable
RUN jar -tf app.jar | grep -q "org/springframework/boot/loader" || (echo "ERROR: Not a Spring Boot executable jar" && exit 1)

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]