# ===== build stage (cache-friendly) =====
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# 1) Copiamos archivos de configuración primero para cachear dependencias
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY settings.gradle.kts ./
COPY build.gradle.kts ./
# (opcional, si existe en tu repo)
COPY gradle.properties ./

RUN chmod +x gradlew
# Warm-up de dependencias y plugins (no falla el build si aún falta src)
RUN ./gradlew --no-daemon --stacktrace build -x test || true

# 2) Ahora el código fuente
COPY src ./src

# Build del jar ejecutable (sin tests para evitar flakiness en contenedor)
RUN ./gradlew clean bootJar -x test --no-daemon --stacktrace

# ===== run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Opcional: instala curl para healthcheck
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# Memoria (Render limita RAM en plan free)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Copiamos el jar (cubre *-SNAPSHOT y releases)
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Healthcheck contra actuator (asegúrate de tener spring-boot-starter-actuator y endpoint expuesto)
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]