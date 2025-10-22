# ===== build stage =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
COPY gradle.properties ./
RUN chmod +x gradlew
RUN ./gradlew --no-daemon clean bootJar

# ===== run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Copia el fat jar
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Render asigna el puerto en $PORT (default 8080 en local)
ENV PORT=8080
EXPOSE 8080

# Healthcheck a Actuator
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Pasa el puerto al arranque
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
