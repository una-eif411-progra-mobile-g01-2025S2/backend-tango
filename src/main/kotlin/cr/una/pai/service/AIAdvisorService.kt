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
import kotlin.random.Random

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
        val shuffledTasks = allTasks.shuffled(java.util.Random(System.currentTimeMillis()))
        val pendingTasks = shuffledTasks.filter { it.status == cr.una.pai.domain.TaskStatus.PENDING || it.status == cr.una.pai.domain.TaskStatus.IN_PROGRESS }
        val completedTasks = shuffledTasks.filter { it.status == cr.una.pai.domain.TaskStatus.COMPLETED }
        val today = java.time.LocalDate.now()
        val upcomingDeadlines = pendingTasks
            .filter { it.deadline != null && it.deadline!!.isBefore(today.plusDays(7)) }
            .sortedBy { it.deadline }
        val pendingTasksContext = pendingTasks.map { task ->
            cr.una.pai.dto.TaskContextInfo(
                title = task.title,
                deadline = task.deadline?.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                priority = task.priority,
                subject = task.subject?.name ?: "(sin materia)",
                status = task.status.name
            )
        }
        val upcomingDeadlinesContext = upcomingDeadlines.map { task ->
            cr.una.pai.dto.TaskContextInfo(
                title = task.title,
                deadline = task.deadline?.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
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
                cr.una.pai.dto.TaskContextInfo(
                    title = task.title,
                    deadline = task.deadline?.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    priority = task.priority,
                    subject = task.subject?.name ?: "(sin materia)",
                    status = task.status.name
                )
            }
        }
        val randomCustomMessage = customMessage ?: "ID de consulta: ${java.util.UUID.randomUUID()}"
        val contexto = cr.una.pai.dto.AIContextData(
            pendingTasks = contextTasks,
            completedTasksCount = completedTasks.size,
            upcomingDeadlines = upcomingDeadlinesContext,
            customMessage = randomCustomMessage
        )
        // Aquí puedes usar adviceStrategy para obtener el consejo
        val consejo = adviceStrategy.getAdvice(userId, allTasks, customMessage)
        // Si necesitas usar el WebClient para IA externa, verifica si está inicializado
        if (webClient != null) {
            // ... lógica para usar WebClient si es necesario ...
        }
        return consejo
    }
    // ...existing code...
}
