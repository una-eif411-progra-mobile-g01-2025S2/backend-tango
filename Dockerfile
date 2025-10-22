# ===== build stage =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiamos wrapper y config primero para cache
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties ./

# (nuevo) Más memoria para Gradle/Kotlin daemon en entornos limitados
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx1g -Dkotlin.daemon.jvm.options=-Xmx512m"

RUN chmod +x gradlew
# (nuevo) Warm-up opcional para descargar dependencias sin fallar el build si falta src
RUN ./gradlew --no-daemon --stacktrace --info -x test build -x bootJar || true

# Copiamos el código después para no invalidar cache de dependencias en cada cambio
COPY src ./src

# (clave) build con logs y sin tests
RUN ./gradlew --no-daemon --stacktrace --info --warning-mode all -x test clean bootJar

# ===== run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
# Copia el jar final (match *-SNAPSHOT o release)
COPY --from=build /app/build/libs/*.jar app.jar

# Render inyecta $PORT
ENV PORT=8080
EXPOSE 8080

# Healthcheck (Actuator)
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Pasa el puerto al arranque
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
