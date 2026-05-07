# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Parent pom
COPY pom.xml .

# Copier les pom des modules
COPY common/pom.xml common/
COPY reporting-service/pom.xml reporting-service/
COPY incident-service/pom.xml incident-service/
COPY notification-service/pom.xml notification-service/
COPY api-gateway/pom.xml api-gateway/

# Télécharger dépendances
RUN mvn dependency:go-offline -pl reporting-service -am -q

# Copier les sources
COPY common common
COPY reporting-service reporting-service

# Build reporting-service
RUN mvn clean package -pl reporting-service -am -DskipTests -q

# Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/reporting-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]