# ===== build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew ./
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src
RUN ./gradlew clean bootJar --no-daemon

# ===== run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
RUN ./gradlew clean bootJar --no-daemon