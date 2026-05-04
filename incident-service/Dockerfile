FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S cloudops && adduser -S cloudops -G cloudops

# On copie uniquement le JAR généré par l'étape Maven de GitHub Actions
COPY target/*.jar app.jar

RUN chown cloudops:cloudops app.jar
USER cloudops
EXPOSE 8082

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]