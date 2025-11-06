# 🛠️ Scripts de Utilidad

Scripts para iniciar, detener y gestionar el backend.

## 📂 Scripts Disponibles

### Iniciar Backend

1. **run-postgres.cmd** ⭐ (Recomendado)
   - Inicia el backend con PostgreSQL
   - Usa perfil `postgres`
   - Los datos persisten entre reinicios

2. **run-default.cmd**
   - Inicia con perfil por defecto (H2 en memoria)
   - ⚠️ Los datos se pierden al reiniciar

3. **run-postgres-no-flyway.cmd**
   - Inicia con PostgreSQL sin ejecutar Flyway
   - Solo para casos especiales

4. **run-local.cmd**
   - Inicia con perfil `local`

6. **test-simple.cmd**
   - Test rápido (health check y status)

### Configuración

7. **setup-postgres.cmd**
   - Configura PostgreSQL automáticamente
   - Crea base de datos y usuario
### AI Testing (Deprecated)

**Períodos Académicos:**
- `consultar-periodos.cmd`
- `crear-periodo.cmd`
- `consultar-periodos-render.cmd`
- `crear-periodo-render.cmd`

**Roles:**
- `consultar-roles.cmd`

**Otros:**
- `verificar-usuario-render.cmd`
- `consultar-materias-render.cmd`
- `consultar-tareas-render.cmd`
- `create-test-data-render.cmd`
- `test-ai-render.cmd`
- `LISTA-SCRIPTS.cmd`

10. **test-ai.cmd / test-ai.ps1 / test-ai-user.cmd / test-ai-user2.cmd**
    - Scripts antiguos de testing de AI
    - Usar endpoints REST en su lugar

## 🚀 Uso Típico

### Primera vez

```bash
# 1. Configurar PostgreSQL
setup-postgres.cmd

# 2. Iniciar backend
run-postgres.cmd

# 3. Verificar que funciona
test-simple.cmd
```

### Desarrollo diario

```bash
# Iniciar backend
run-postgres.cmd

# En otra terminal, hacer cambios...

# Detener
stop-server.cmd

# Reiniciar
run-postgres.cmd
```

### Testing

```bash
# Test rápido
test-simple.cmd

# Test completo
test-backend.cmd
```

## ⚙️ Configuración de Scripts

Los scripts configuran estas variables de entorno:

```batch
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/backend_tango_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=cdcd1903
```

## 📝 Notas

- Todos los scripts deben ejecutarse desde la raíz del proyecto
- Para PowerShell, usar archivos `.ps1`
- Para CMD, usar archivos `.cmd`

## 🔗 Ver También

- `../docs/setup/` - Guías de configuración
- `../docs/database/` - Scripts SQL

