FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy entire multi-module project
COPY . .

# Build only this service, but with parent available
RUN mvn -pl kitchen-service -am clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app
ENV PORT=8080

COPY --from=build /build/kitchen-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
