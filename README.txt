# 📚 Backend Tango

Proyecto académico desarrollado con **Spring Boot + Kotlin + PostgreSQL**.
Este backend se conecta a una base de datos Postgres llamada `paiapp`.

---

## ✅ Requisitos de instalación
🔹 1. Java 17

🔹 2. Gradle

🔹 3. PostgreSQL

🔹 4. Instalar también pgAdmin 4

    Crear la base de datos y usuario desde pgAdmin 4
    usuario:
    Name: paiapp
    password: 12345

    base:
    Database paiapp
    Owner paiapp

🚀 Cómo ejecutar el proyecto

1. Compilar y ejecutar tests

gradlew clean build
2. Levantar la aplicación

gradlew bootRun
La app se iniciará en:
http://localhost:8080

resultado:
{"status":"ok"}

3. test2
gradlew test --tests "cr.una.pai.ConnectivityTest"
Resultado: BUILD SUCCESSFUL