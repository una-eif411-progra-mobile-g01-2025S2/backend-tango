# syntax=docker/dockerfile:1

# -------- Build stage --------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Copiamos solo lo necesario para aprovechar la caché de Docker
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src

# Normaliza fin de línea por si el wrapper está en CRLF (Windows) y marca como ejecutable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Construye el JAR ejecutable (omitimos tests para acelerar la imagen)
RUN ./gradlew --no-daemon clean bootJar -x test

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-jammy AS runtime

ENV APP_HOME=/opt/app
WORKDIR ${APP_HOME}

# Usuario no-root para ejecutar la app
RUN useradd -ms /bin/bash appuser

# Copiamos el artefacto construido
COPY --from=build /workspace/build/libs/app.jar app.jar

# Copiamos el script de entrada y aseguramos permisos
COPY entrypoint.sh ./entrypoint.sh
RUN sed -i 's/\r$//' ./entrypoint.sh && chmod +x ./entrypoint.sh && chown -R appuser:appuser ${APP_HOME}
USER appuser

# Puerto por defecto local; Render inyecta PORT en runtime
ENV PORT=8080
EXPOSE 8080

# Flags JVM seguros para contenedores
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

# Usamos un script simple como entrypoint (evita problemas de quoting en plataformas de despliegue)
ENTRYPOINT ["/opt/app/entrypoint.sh"]