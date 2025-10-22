# ===== build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Wrapper + permisos
COPY gradlew ./
RUN chmod +x gradlew
COPY gradle gradle

# Archivos de build (sin el código aún para aprovechar caché)
COPY settings.gradle.kts build.gradle.kts ./
# Si tienes gradle.properties en la raíz, descomenta la siguiente línea:
# COPY gradle.properties ./

# (Opcional seguro) Crear gradle.properties mínimo si no existe, útil en CI con poca RAM
RUN bash -lc 'test -f gradle.properties || printf "org.gradle.daemon=false\norg.gradle.parallel=true\norg.gradle.configureondemand=true\norg.gradle.jvmargs=-Xmx1g -XX:+UseParallelGC -Dkotlin.daemon.jvm.options=-Xmx512m\nkotlin.incremental=true\nkapt.use.worker.api=true\n" > gradle.properties'

# Mostrar versiones para diagnósticos
RUN ./gradlew --no-daemon --version

# Copiar el código
COPY src src

# Compilar con detalle (si falla aquí verás clase/línea)
RUN ./gradlew clean compileKotlin --no-daemon --stacktrace --info --warning-mode all

# Empaquetar el JAR
RUN ./gradlew bootJar --no-daemon --stacktrace --info --warning-mode all


# ===== run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# curl para healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Ajuste de memoria recomendado en contenedores
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Copiar el jar generado (comodín para SNAPSHOT o release)
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Puerto por defecto de Spring Boot
EXPOSE 8080

# Healthcheck a Actuator (asegúrate de tener el starter de actuator)
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# Ejecutar
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
