# ── Stage 1 : Build Maven ──────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Copie le pom parent + modules nécessaires
COPY pom.xml ./
COPY common/pom.xml common/
COPY reporting-service/pom.xml reporting-service/

# Pre-télécharge les dépendances (layer cacheable)
RUN mvn dependency:go-offline -pl reporting-service -am -q

# Copie les sources
COPY common/src common/src
COPY reporting-service/src reporting-service/src

# Build sans tests (les tests tournent dans CI)
RUN mvn clean package -pl reporting-service -am -DskipTests -q

# ── Stage 2 : Runtime JRE minimal ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root (sécurité)
RUN addgroup -S cloudops && adduser -S cloudops -G cloudops
USER cloudops

# Copie le JAR depuis le stage builder
COPY --from=builder /build/reporting-service/target/reporting-service-*.jar app.jar

# Port du service
EXPOSE 8084

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
