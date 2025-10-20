# 🤖 Servicio de IA - Asesor Personal de Productividad

## Descripción
El servicio de IA proporciona consejos personalizados y recordatorios a los estudiantes basándose en sus tareas pendientes, fechas límite próximas y progreso académico.

## Endpoints Disponibles

### 1. Obtener Consejo por POST
**URL:** `POST /api/v1/ai-advisor/advice`

**Body (JSON):**
```json
{
  "userId": "uuid-del-usuario",
  "customMessage": "Mensaje opcional del estudiante"
}
```

**Ejemplo de uso:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "customMessage": "Necesito ayuda para organizar mi semana"
}
```

**Respuesta:**
```json
{
  "advice": "¡Hola! Tienes 3 tareas pendientes con fechas límite próximas...",
  "timestamp": "2025-10-19T10:30:00"
}
```

### 2. Obtener Consejo por GET
**URL:** `GET /api/v1/ai-advisor/advice/{userId}`

**Ejemplo:** `GET /api/v1/ai-advisor/advice/123e4567-e89b-12d3-a456-426614174000`

**Respuesta:**
```json
{
  "advice": "¡Hola! Tienes 3 tareas pendientes con fechas límite próximas...",
  "timestamp": "2025-10-19T10:30:00"
}
```

## Características

✅ **Análisis de Tareas Pendientes:** La IA analiza todas las tareas del usuario y su estado.

✅ **Alertas de Fechas Límite:** Identifica tareas con fechas límite en los próximos 7 días.

✅ **Priorización:** Considera la prioridad de cada tarea para dar mejores consejos.

✅ **Contexto Académico:** Incluye información sobre las materias asociadas a cada tarea.

✅ **Motivación:** Proporciona palabras de motivación y recordatorios útiles.

## Configuración

La API Key de DeepSeek está configurada en el archivo `AIAdvisorService.kt`. Si necesitas cambiarla:

1. Abre `src/main/kotlin/cr/una/pai/service/AIAdvisorService.kt`
2. Modifica la variable `apiKey`

## Ejemplo de Uso desde el Frontend

```javascript
// Obtener consejo de IA
const obtenerConsejo = async (userId) => {
  const response = await fetch(`http://localhost:8080/api/v1/ai-advisor/advice/${userId}`);
  const data = await response.json();
  console.log(data.advice);
};

// O con mensaje personalizado
const obtenerConsejoConMensaje = async (userId, mensaje) => {
  const response = await fetch('http://localhost:8080/api/v1/ai-advisor/advice', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: userId,
      customMessage: mensaje
    })
  });
  const data = await response.json();
  console.log(data.advice);
};
```

## Cómo Funciona Internamente

1. **Recopilación de Datos:** El servicio obtiene todas las tareas del usuario desde la base de datos.

2. **Análisis de Contexto:** Identifica:
   - Tareas pendientes vs completadas
   - Tareas con fechas límite próximas (próximos 7 días)
   - Prioridades de cada tarea
   - Materias asociadas

3. **Construcción del Prompt:** Crea un mensaje contextualizado para la IA con toda la información relevante.

4. **Llamada a DeepSeek API:** Envía el contexto a la API de DeepSeek para generar el consejo.

5. **Respuesta:** Retorna el consejo generado por la IA al cliente.

## Archivos Creados

- `dto/AIAdvisorDTO.kt` - Modelos de datos para requests/responses
- `service/AIAdvisorService.kt` - Lógica de negocio y consumo de API
- `web/AIAdvisorController.kt` - Controlador REST

## Dependencias Agregadas

- `org.json:json:20231013` - Para manejo de JSON en las peticiones a la API

## Notas Importantes

⚠️ **API Key:** La API key está hardcoded en el servicio. En producción, considera moverla a variables de entorno o a `application.properties`.

⚠️ **Rate Limits:** DeepSeek tiene límites de uso. Considera implementar caché o throttling si el uso es muy frecuente.

⚠️ **Manejo de Errores:** El servicio tiene manejo básico de errores. Considera mejorar la gestión de excepciones según tus necesidades.

