# ===== build stage =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew ./
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src
RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

# ===== run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
# Usa un puerto no root friendly o configura USER
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
