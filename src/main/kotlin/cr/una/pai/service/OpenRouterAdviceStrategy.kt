package cr.una.pai.service

import cr.una.pai.dto.AIContextData
import cr.una.pai.dto.AIAdvisorResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.*

@Component
class OpenRouterAdviceStrategy(
    @Value("\${openrouter.api.key}") private val apiKey: String,
    @Value("\${openrouter.api.url}") private val apiUrl: String,
    @Value("\${openrouter.model}") private val model: String,
    @Value("\${openrouter.temperature:1.0}") private val temperature: Double
) : AdviceStrategy {

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(apiUrl)
        .defaultHeader("Authorization", "Bearer $apiKey")
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("HTTP-Referer", "http://localhost:8080")
        .defaultHeader("X-Title", "PAI Backend - AI Advisor")
        .build()

    override fun generarConsejo(contexto: AIContextData): AIAdvisorResponse {
        // Log para depuración: mostrar el contexto recibido
        println("[OpenRouterAdviceStrategy] Contexto recibido: pendingTasks=${contexto.pendingTasks.size}, customMessage=${contexto.customMessage}")
        contexto.pendingTasks.forEach { println("[OpenRouterAdviceStrategy] Tarea: ${it.title}, ${it.subject}, ${it.deadline}, ${it.status}") }
        val prompt = construirPrompt(contexto)
        println("[OpenRouterAdviceStrategy] Prompt generado para IA:\n$prompt")
        val advice = llamarAPIOpenRouter(prompt)
        return AIAdvisorResponse(advice = advice)
    }

    private fun construirPrompt(contexto: AIContextData): String {
        val sb = StringBuilder()
        sb.append("Da un recordatorio concreto para cada tarea listada. No incluyas motivación ni explicaciones generales.\n\n")
        sb.append("Tareas:\n")
        if (contexto.pendingTasks.isNotEmpty()) {
            contexto.pendingTasks.forEach { task ->
                sb.append("- ${task.title} (${task.subject ?: "(sin materia)"}")
                if (task.deadline != null) sb.append(", vence ${task.deadline}")
                sb.append(")\n")
            }
            // Solo agregar mensaje personalizado si NO es un UUID autogenerado y hay tareas
            if (!contexto.customMessage.isNullOrBlank() && !contexto.customMessage.startsWith("ID de consulta:")) {
                sb.append("\nMensaje personalizado para la IA:\n")
                sb.append(contexto.customMessage)
                sb.append("\n")
            }
        } else {
            sb.append("(No hay tareas pendientes para este usuario.)\n")
            // No agregar mensaje personalizado ni ninguna otra sección
        }
        return sb.toString()
    }

    private fun llamarAPIOpenRouter(contexto: String): String {
        return try {
            val requestBody = mapOf(
                "model" to model,
                "messages" to listOf(
                    mapOf("role" to "user", "content" to contexto)
                ),
                "temperature" to temperature,
                "max_tokens" to 512
            )
            val response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono<Map<String, Any>>()
                .block()
            if (response != null) {
                val choices = response["choices"] as? List<*>
                if (choices != null && choices.isNotEmpty()) {
                    val firstChoice = choices[0] as? Map<*, *>
                    val message = firstChoice?.get("message") as? Map<*, *>
                    val content = message?.get("content") as? String
                    return content ?: "No se obtuvo respuesta del modelo"
                }
            }
            "Error al obtener respuesta de la IA"
        } catch (e: Exception) {
            "Lo siento, no pude generar un consejo en este momento. Error: ${e.message}"
        }
    }
}
