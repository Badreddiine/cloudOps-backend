FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml                common/pom.xml
COPY api-gateway/pom.xml           api-gateway/pom.xml
COPY incident-service/pom.xml      incident-service/pom.xml
COPY notification-service/pom.xml  notification-service/pom.xml
COPY reporting-service/pom.xml     reporting-service/pom.xml

RUN mvn dependency:go-offline -B

COPY common/src               common/src
COPY notification-service/src notification-service/src

RUN mvn clean package -Dmaven.test.skip=true -B -pl notification-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S cloudops && adduser -S cloudops -G cloudops
COPY --from=builder /app/notification-service/target/*.jar app.jar
RUN chown cloudops:cloudops app.jar
USER cloudops
EXPOSE 8083
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]