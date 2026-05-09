# ─────────────────────────────────────────
# Stage 1 : Build
# ─────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml                common/pom.xml
COPY incident-service/pom.xml      incident-service/pom.xml
COPY notification-service/pom.xml  notification-service/pom.xml
COPY api-gateway/pom.xml           api-gateway/pom.xml

RUN mvn dependency:go-offline -B

COPY common/src           common/src
COPY incident-service/src incident-service/src

RUN mvn clean package -Dmaven.test.skip=true -B \
    -pl incident-service -am

# ─────────────────────────────────────────
# Stage 2 : Run
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S cloudops && adduser -S cloudops -G cloudops

COPY --from=builder /app/incident-service/target/*.jar app.jar

RUN chown cloudops:cloudops app.jar
USER cloudops
EXPOSE 8082

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]