package cr.una.pai.service

import cr.una.pai.domain.TaskStatus
import cr.una.pai.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@Transactional
class AIAdvisorService(
    private val taskService: TaskService,
    @Value("\${openrouter.api.key:}") private val apiKey: String,
    @Value("\${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}") private val apiUrl: String,
    @Value("\${openrouter.model:tngtech/deepseek-r1t2-chimera:free}") private val model: String
) {

    private val webClient: WebClient? = if (apiKey.isNotBlank()) {
        WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Authorization", "Bearer $apiKey")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("HTTP-Referer", "http://localhost:8080")
            .defaultHeader("X-Title", "PAI Backend - AI Advisor")
            .build()
    } else {
        null
    }

    fun obtenerConsejosParaUsuario(userId: UUID, customMessage: String? = null): AIAdvisorResponse {
        // Obtener las tareas del usuario (con materia asociada)
        val allTasks = taskService.findAllByUserIdWithSubject(userId)
        println("[AIAdvisor] Tareas obtenidas para el usuario: ${allTasks.size}")
        allTasks.forEach { println("[AIAdvisor] Tarea: ${it.title}, status: ${it.status}, deadline: ${it.deadline}, subject: ${it.subject?.name}") }
        val pendingTasks = allTasks.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }
        val completedTasks = allTasks.filter { it.status == TaskStatus.COMPLETED }

        // Tareas con deadline próximo (en los próximos 7 días)
        val today = LocalDate.now()
        val upcomingDeadlines = pendingTasks
            .filter { it.deadline != null && it.deadline!!.isBefore(today.plusDays(7)) }
            .sortedBy { it.deadline }

        // Construir contexto para la IA
        val pendingTasksContext = pendingTasks.map { task ->
            TaskContextInfo(
                title = task.title,
                deadline = task.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                priority = task.priority,
                subject = task.subject?.name ?: "(sin materia)",
                status = task.status.name
            )
        }
        println("[AIAdvisor] Tareas mapeadas a TaskContextInfo: ${pendingTasksContext.size}")
        pendingTasksContext.forEach { println("[AIAdvisor] TaskContextInfo: ${it.title}, ${it.subject}, ${it.deadline}, ${it.status}") }
        val upcomingDeadlinesContext = upcomingDeadlines.map { task ->
            TaskContextInfo(
                title = task.title,
                deadline = task.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                priority = task.priority,
                subject = task.subject?.name ?: "(sin materia)",
                status = task.status.name
            )
        }
        val contexto = construirContexto(
            pendingTasks = pendingTasksContext,
            completedTasksCount = completedTasks.size,
            upcomingDeadlines = upcomingDeadlinesContext,
            customMessage = customMessage
        )

        // Llamar a la API de OpenRouter con DeepSeek R1
        val advice = llamarAPIOpenRouter(contexto)

        // Si la IA falla, mostrar tareas urgentes/prioritarias
        if (advice.isNullOrBlank() || advice.startsWith("Lo siento") || advice.contains("Error")) {
            val sb = StringBuilder()
            sb.append("No se pudo obtener un consejo automático. Aquí tienes tus tareas más importantes:\n\n")
            val tareasMostradas = mutableListOf<TaskContextInfo>()
            tareasMostradas.addAll(upcomingDeadlines.take(3).map { task ->
                TaskContextInfo(
                    title = task.title,
                    deadline = task.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    priority = task.priority,
                    subject = task.subject?.name ?: "(sin materia)",
                    status = task.status.name
                )
            })
            tareasMostradas.addAll(pendingTasks.filter { t -> upcomingDeadlines.none { it.title == t.title } }
                .sortedByDescending { it.priority }
                .take(2)
                .map { task ->
                    TaskContextInfo(
                        title = task.title,
                        deadline = task.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        priority = task.priority,
                        subject = task.subject?.name ?: "(sin materia)",
                        status = task.status.name
                    )
                })
            tareasMostradas.forEach { task ->
                sb.append("- ${task.title} (${task.subject}")
                if (task.deadline != null) sb.append(", vence ${task.deadline}")
                sb.append(")\n")
            }
            return AIAdvisorResponse(advice = sb.toString())
        }

        return AIAdvisorResponse(advice = advice)
    }

    private fun construirContexto(
        pendingTasks: List<TaskContextInfo>,
        completedTasksCount: Int,
        upcomingDeadlines: List<TaskContextInfo>,
        customMessage: String?
    ): String {
        val sb = StringBuilder()
        sb.append("Da un recordatorio concreto para cada tarea listada. No incluyas motivación ni explicaciones generales.\n\n")
        sb.append("Tareas:\n")
        // Si hay tareas urgentes, mostrar hasta 3; si no, mostrar todas las pendientes
        val tareasMostradas = if (upcomingDeadlines.isNotEmpty()) {
            mutableListOf<TaskContextInfo>().apply {
                addAll(upcomingDeadlines.take(3))
                addAll(pendingTasks.filter { t -> upcomingDeadlines.none { it.title == t.title } }.sortedByDescending { it.priority }.take(2))
            }
        } else {
            pendingTasks
        }
        tareasMostradas.forEach { task ->
            sb.append("- ${task.title} (${task.subject}")
            if (task.deadline != null) sb.append(", vence ${task.deadline}")
            sb.append(")\n")
        }
        return sb.toString()
    }

    private fun llamarAPIOpenRouter(contexto: String): String? {
        if (webClient == null) {
            return "El servicio de asesoría inteligente está deshabilitado porque la API key de OpenRouter no está configurada. Configura el valor 'OPENROUTER_API_KEY' para activar esta funcionalidad."
        }
        println("[AIAdvisor] Contexto enviado a IA:\n$contexto")
        return try {
            val requestBody = mapOf(
                "model" to model,
                "messages" to listOf(
                    mapOf("role" to "user", "content" to contexto)
                ),
                "temperature" to 0.7,
                "max_tokens" to 512 // Aumentado para permitir respuestas más largas
            )
            println("[AIAdvisor] RequestBody: $requestBody")
            val response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .block()
            println("[AIAdvisor] Respuesta de la IA: $response")
            if (response != null) {
                val choices = response["choices"] as? List<*>
                if (choices != null && choices.isNotEmpty()) {
                    val firstChoice = choices[0] as? Map<*, *>
                    val message = firstChoice?.get("message") as? Map<*, *>
                    val content = message?.get("content") as? String
                    println("[AIAdvisor] Contenido generado por la IA: $content")
                    return content ?: "No se obtuvo respuesta del modelo"
                }
            }
            println("[AIAdvisor] Error: Respuesta de la IA vacía o sin choices")
            "Error al obtener respuesta de la IA"
        } catch (e: Exception) {
            println("[AIAdvisor] Error al llamar a la API de OpenRouter: ${e.message}")
            e.printStackTrace()
            "Lo siento, no pude generar un consejo en este momento. Error: ${e.message}"
        }
    }
}
