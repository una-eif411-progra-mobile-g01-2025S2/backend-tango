package cr.una.pai.service

import cr.una.pai.domain.TaskStatus
import cr.una.pai.dto.AIAdvisorResponse
import cr.una.pai.dto.AIContextData
import cr.una.pai.dto.TaskContextInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Random
import java.util.UUID

@Service
@Transactional
class AIAdvisorService(
    private val taskService: TaskService,
    private val adviceStrategy: AdviceStrategy, // Inyección para flexibilidad y pruebas
    @Value("\${openrouter.api.key:}") private val apiKey: String,
    @Value("\${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}") private val apiUrl: String,
    @Value("\${openrouter.model:tngtech/deepseek-r1t2-chimera:free}") private val model: String,
    @Value("\${app.base-url:http://localhost:8080}") private val appBaseUrl: String
) {
    // Inicialización condicional de WebClient
    private val webClient: WebClient? = if (apiKey.isNotBlank()) {
        WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Authorization", "Bearer $apiKey")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("HTTP-Referer", appBaseUrl)
            .defaultHeader("X-Title", "PAI Backend - AI Advisor")
            .build()
    } else {
        null
    }

    fun obtenerConsejosParaUsuario(userId: UUID, customMessage: String? = null): AIAdvisorResponse {
        val allTasks = taskService.findAllByUserIdWithSubject(userId)
        println("[AIAdvisor] Tareas obtenidas para el usuario $userId: ${allTasks.size}")
        allTasks.forEach { println("[AIAdvisor] Tarea: ${it.title}, status: ${it.status}, deadline: ${it.deadline}, subject: ${it.subject?.name}") }
        val shuffledTasks = allTasks.shuffled(Random(System.currentTimeMillis()))
        val pendingTasks = shuffledTasks.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }
        val completedTasks = shuffledTasks.filter { it.status == TaskStatus.COMPLETED }
        val today = LocalDate.now()
        val upcomingDeadlines = pendingTasks
            .filter { it.deadline != null && it.deadline!!.isBefore(today.plusDays(7)) }
            .sortedBy { it.deadline }
        val pendingTasksContext = pendingTasks.map { task ->
            TaskContextInfo(
                title = task.title,
                deadline = task.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                priority = task.priority,
                subject = task.subject?.name ?: "(sin materia)",
                status = task.status.name
            )
        }
        val upcomingDeadlinesContext = upcomingDeadlines.map { task ->
            TaskContextInfo(
                title = task.title,
                deadline = task.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                priority = task.priority,
                subject = task.subject?.name ?: "(sin materia)",
                status = task.status.name
            )
        }
        // Si no hay tareas pendientes, usar próximas o completadas para que la IA siempre reciba algo
        val contextTasks = if (pendingTasksContext.isNotEmpty()) {
            pendingTasksContext
        } else if (upcomingDeadlinesContext.isNotEmpty()) {
            upcomingDeadlinesContext
        } else {
            completedTasks.map { task ->
                TaskContextInfo(
                    title = task.title,
                    deadline = task.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    priority = task.priority,
                    subject = task.subject?.name ?: "(sin materia)",
                    status = task.status.name
                )
            }
        }
        val randomCustomMessage = customMessage ?: "ID de consulta: ${UUID.randomUUID()}"
        val contexto = AIContextData(
            pendingTasks = contextTasks,
            completedTasksCount = completedTasks.size,
            upcomingDeadlines = upcomingDeadlinesContext,
            customMessage = randomCustomMessage
        )
        // Aquí puedes usar adviceStrategy para obtener el consejo
        val consejo = adviceStrategy.generarConsejo(contexto)
        // Si necesitas usar el WebClient para IA externa, verifica si está inicializado
        if (webClient != null) {
            // ... lógica para usar WebClient si es necesario ...
        }
        return consejo
    }
    // ...existing code...
}
