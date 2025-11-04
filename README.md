 # 🎓 PAI Backend - Sistema de Planificación Académica

Backend desarrollado con **Spring Boot + Kotlin** para gestión de tareas académicas y planificación de estudio.

## 📊 Stack Tecnológico

- **Backend:** Spring Boot 3.2.5 + Kotlin 1.9.23
- **Base de Datos:** PostgreSQL 18
- **Seguridad:** JWT Authentication
- **ORM:** JPA/Hibernate
- **Migraciones:** Flyway
- **Despliegue:** Docker + Render

---

## 📂 Estructura del Proyecto

```
backend-tango/
├── src/                          # Código fuente del BACKEND (Spring Boot + Kotlin)
│   ├── main/
│   │   ├── kotlin/cr/una/pai/    # Código Kotlin del BACKEND
│   │   │   ├── domain/           # Entidades y Repositorios
│   │   │   ├── dto/              # DTOs
│   │   │   ├── mapper/           # Mappers (MapStruct)
│   │   │   ├── security/         # JWT y configuración de seguridad
│   │   │   ├── service/          # Lógica de negocio
│   │   │   └── web/              # Controladores REST
│   │   └── resources/
│   │       ├── application.properties           # Config general
│   │       ├── application-postgres.properties  # Config PostgreSQL
│   │       └── db/migration/postgres/           # Scripts Flyway
│   └── test/                     # Tests
│
├── docs/                         # Documentación
│   ├── android/                  # ⚠️ REFERENCIA para proyecto Android
│   │                             # (Archivos .kt para copiar a Android Studio)
│   ├── database/                 # Scripts SQL y documentación BD
│   ├── setup/                    # Guías de configuración
│   ├── AI_ADVISOR.md             # Documentación AI Advisor
│   └── postman_collection.json   # Colección Postman
│
├── scripts/                      # Scripts de utilidad
│   ├── run-postgres.cmd          # Iniciar con PostgreSQL
│   ├── setup-postgres.cmd        # Configurar PostgreSQL
│   ├── test-backend.cmd          # Test del backend
│   └── stop-server.cmd           # Detener servidor
│
├── build.gradle.kts              # Configuración Gradle
├── Dockerfile                    # Imagen Docker
├── render.yaml                   # Configuración Render
└── README.md                     # Este archivo
```

⚠️ **Nota:** La carpeta `docs/android/` contiene archivos de **referencia** para tu proyecto Android. Son ejemplos de código Kotlin para Android Studio, NO forman parte del backend.

---

## 🚀 Inicio Rápido

### Requisitos Previos

- Java 17
- PostgreSQL 18
- Gradle 8.14.3

### 1. Configurar PostgreSQL

```bash
# Ejecutar script de configuración
cd scripts
.\setup-postgres.cmd
```

O configurar manualmente:
- Base de datos: `backend_tango_db`
- Usuario: `postgres`
- Contraseña: `cdcd1903`
- Puerto: `5432`

### 2. Insertar Datos de Prueba

En pgAdmin, ejecutar: `docs/database/INSERTAR_DATOS_PRUEBA.sql`

**Usuarios de prueba:**
- `cris@gmail.com` / `password123`
- `maria@test.com` / `password123`
- `carlos@test.com` / `password123`

### 3. Iniciar el Backend

```bash
cd scripts
.\run-postgres.cmd
```

El backend estará disponible en: `http://localhost:8080`

### 4. Verificar Funcionamiento

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada: `{"status":"UP"}`

---

## 🌐 Endpoints Principales

### Authentication
```
POST   /api/v1/users/signup      - Registro
POST   /api/v1/auth/login        - Login
GET    /api/v1/users/me          - Usuario actual
```

### Academic Periods
```
GET    /api/v1/periods           - Listar períodos
POST   /api/v1/periods           - Crear período
```

### Subjects (Materias)
```
GET    /api/v1/subjects                          - Listar materias
GET    /api/v1/subjects/user/{userId}            - Materias por usuario
POST   /api/v1/subjects                          - Crear materia
PUT    /api/v1/subjects/{id}                     - Actualizar
DELETE /api/v1/subjects/{id}                     - Eliminar
```

### Tasks (Tareas)
```
GET    /api/v1/tasks             - Listar tareas
POST   /api/v1/tasks             - Crear tarea
PUT    /api/v1/tasks/{id}        - Actualizar
DELETE /api/v1/tasks/{id}        - Eliminar
```

### AI Advisor
```
GET    /api/v1/ai-advisor/advice/{userId}        - Obtener consejos
POST   /api/v1/ai-advisor/advice                 - Obtener consejos (POST)
```

### Utilidades
```
GET    /api/v1/utils/status                      - Estado del sistema
POST   /api/v1/utils/init-all-data               - Inicializar datos de prueba
POST   /api/v1/utils/init-users                  - Crear usuarios de prueba
POST   /api/v1/utils/init-periods                - Crear períodos de prueba
```

---

## 📱 Integración con Android

Ver guías en: [`docs/android/`](docs/android/)

**Archivos clave:**
- `ANDROID_RetrofitClient.kt` - Cliente HTTP configurado
- `ANDROID_ApiService.kt` - Interface con endpoints
- `ANDROID_Models.kt` - Modelos de datos
- `CONECTA_ANDROID_YA.md` - Guía de conexión

**URLs para Android:**
- Emulador: `http://10.0.2.2:8080/`
- Dispositivo físico: `http://192.168.100.6:8080/`
- Producción (Render): `https://tu-app.onrender.com/`

---

## 🗄️ Base de Datos

### Tablas Principales

- `app_user` - Usuarios del sistema
- `academic_period` - Períodos académicos
- `subject` - Materias/Asignaturas
- `task` - Tareas de los estudiantes
- `study_block` - Bloques de estudio planificados
- `weekly_availability` - Disponibilidad semanal

### Scripts Útiles

```bash
# Ver estructura de tablas
docs/database/TEST_BD.sql

# Insertar datos de prueba
docs/database/INSERTAR_DATOS_PRUEBA.sql

# Limpiar base de datos
docs/database/LIMPIAR_BD_PARA_FLYWAY.sql
```

### Gestión con pgAdmin

1. Abrir pgAdmin 4
2. Conectar a `backend_tango_db` (password: `cdcd1903`)
3. Ver tablas: `Schemas` → `public` → `Tables`

---

## 🔧 Scripts Disponibles

### Iniciar Backend
```bash
scripts/run-postgres.cmd              # Con PostgreSQL
scripts/run-default.cmd               # Perfil por defecto
```

### Testing
```bash
scripts/test-backend.cmd              # Test completo
scripts/test-simple.cmd               # Test rápido
```

### Utilidades
```bash
scripts/setup-postgres.cmd            # Configurar PostgreSQL
scripts/stop-server.cmd               # Detener servidor
```

---

## 🐳 Despliegue con Docker

### Build local
```bash
docker build -t backend-tango .
docker run -p 8080:8080 backend-tango
```

### Desplegar en Render

El proyecto está configurado con `render.yaml`. Solo necesitas:

1. Subir código a GitHub
2. Conectar repo en Render
3. Render detecta `render.yaml` automáticamente
4. Despliega con PostgreSQL incluido

Ver: `CAMBIOS_RENDER.md` para más detalles

---

## 📚 Documentación Adicional

### Setup y Configuración
- [`docs/setup/3_PASOS_PGADMIN.md`](docs/setup/3_PASOS_PGADMIN.md) - Configuración pgAdmin
- [`docs/setup/CONFIGURACION_ENTORNOS.md`](docs/setup/CONFIGURACION_ENTORNOS.md) - Entornos

### Android
- [`docs/android/CONECTA_ANDROID_YA.md`](docs/android/CONECTA_ANDROID_YA.md) - Conexión rápida
- [`docs/android/ANDROID_EJEMPLOS.md`](docs/android/ANDROID_EJEMPLOS.md) - Ejemplos de código

### Base de Datos
- [`docs/database/TEST_BD.sql`](docs/database/TEST_BD.sql) - Script de verificación
- [`docs/database/INSERTAR_DATOS_PRUEBA.sql`](docs/database/INSERTAR_DATOS_PRUEBA.sql) - Datos de prueba

### General
- [`docs/AI_ADVISOR.md`](docs/AI_ADVISOR.md) - Documentación AI Advisor
- [`docs/postman_collection.json`](docs/postman_collection.json) - Colección Postman
- [`STACK_COMPLETO.md`](STACK_COMPLETO.md) - Arquitectura completa

---

## 🛠️ Desarrollo

### Compilar
```bash
.\gradlew.bat compileKotlin
```

### Construir JAR
```bash
.\gradlew.bat bootJar
```

### Ejecutar Tests
```bash
.\gradlew.bat test
```

---

## 🔐 Seguridad

- Autenticación JWT
- Refresh tokens
- Contraseñas hasheadas con BCrypt
- CORS configurado
- Endpoints públicos: `/api/v1/users/signup`, `/api/v1/auth/login`, `/actuator/health`

---

## 🤝 Contribuir

1. Fork el proyecto
2. Crear branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

---

## 📝 Licencia

Este proyecto es parte del curso de Programación Móvil - Universidad de Costa Rica

---

## 👥 Equipo

Desarrollado por estudiantes de Ingeniería en Sistemas - UNA

---

## 📞 Soporte

Para problemas o preguntas:
1. Revisar documentación en `docs/`
2. Verificar scripts en `scripts/`
3. Consultar logs del backend

---

**Estado:** ✅ En desarrollo activo  
**Versión:** 0.0.1-SNAPSHOT  
**Última actualización:** 2025-11-04
mos usando