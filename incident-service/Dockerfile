# ─────────────────────────────────────────
# Stage 1 : Build
# ─────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 1. Copier le POM parent
COPY pom.xml .

# 2. Copier les POMs de TOUS les modules
COPY common/pom.xml                common/pom.xml
COPY api-gateway/pom.xml           api-gateway/pom.xml
COPY incident-service/pom.xml      incident-service/pom.xml
COPY notification-service/pom.xml  notification-service/pom.xml
COPY reporting-service/pom.xml     reporting-service/pom.xml

# 3. Telecharger les dependances en cache
RUN mvn dependency:go-offline -B

# 4. Copier les sources
COPY common/src      common/src
COPY api-gateway/src api-gateway/src

# 5. Builder
RUN mvn clean package -Dmaven.test.skip=true -B \
    -pl api-gateway -am

# ─────────────────────────────────────────
# Stage 2 : Run
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S cloudops && adduser -S cloudops -G cloudops

COPY --from=builder /app/api-gateway/target/*.jar app.jar

RUN chown cloudops:cloudops app.jar

USER cloudops

EXPOSE 8080

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]