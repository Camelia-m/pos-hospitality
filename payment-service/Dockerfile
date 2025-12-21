FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy the entire project structure
COPY . .

# First, install the parent POM
RUN mvn -N install

# Then build and package payment-service specifically
RUN cd payment-service && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
ENV PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Copy the executable jar
COPY --from=build /build/payment-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]