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
    @Value("\${openrouter.fallback.models:}") private val fallbackModelsStr: String,
    @Value("\${openrouter.temperature:1.0}") private val temperature: Double
) : AdviceStrategy {

    private val fallbackModels: List<String> = if (fallbackModelsStr.isNotBlank()) {
        fallbackModelsStr.split(",").map { it.trim() }
    } else {
        emptyList()
    }

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

        // Si no hay tareas, devolver un mensaje amigable sin llamar a la API
        if (contexto.pendingTasks.isEmpty() && contexto.upcomingDeadlines.isEmpty()) {
            println("[OpenRouterAdviceStrategy] Usuario sin tareas. Devolviendo mensaje de bienvenida.")
            return AIAdvisorResponse(
                advice = "¡Bienvenido! 🎓\n\n" +
                        "Aún no tienes tareas registradas. Para comenzar:\n\n" +
                        "1. Crea tus materias académicas\n" +
                        "2. Agrega tus tareas y exámenes\n" +
                        "3. Vuelve aquí para recibir consejos personalizados\n\n" +
                        "¡Estoy aquí para ayudarte a organizar tu estudio!"
            )
        }

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
        // Lista de modelos a intentar: primero el principal, luego los de fallback
        val modelsToTry = listOf(model) + fallbackModels

        println("[OpenRouterAdviceStrategy] Modelos disponibles: ${modelsToTry.size}")
        println("[OpenRouterAdviceStrategy] Modelo principal: $model")
        if (fallbackModels.isNotEmpty()) {
            println("[OpenRouterAdviceStrategy] Modelos de fallback: ${fallbackModels.joinToString(", ")}")
        }

        // Intentar con cada modelo en orden
        modelsToTry.forEachIndexed { index, currentModel ->
            try {
                val requestBody = mapOf(
                    "model" to currentModel,
                    "messages" to listOf(
                        mapOf("role" to "user", "content" to contexto)
                    ),
                    "temperature" to temperature,
                    "max_tokens" to 512
                )

                println("[OpenRouterAdviceStrategy] Intento ${index + 1}/${modelsToTry.size}: Llamando a OpenRouter API...")
                println("[OpenRouterAdviceStrategy] Modelo: $currentModel")
                println("[OpenRouterAdviceStrategy] API URL: $apiUrl")
                println("[OpenRouterAdviceStrategy] API Key configurada: ${if (apiKey.isNotBlank()) "Sí (${apiKey.take(10)}...)" else "No"}")

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
                        if (!content.isNullOrBlank()) {
                            println("[OpenRouterAdviceStrategy] ✅ Respuesta exitosa con modelo: $currentModel")
                            return content
                        }
                    }
                }
                println("[OpenRouterAdviceStrategy] ⚠️ Respuesta vacía del modelo: $currentModel")

            } catch (e: org.springframework.web.reactive.function.client.WebClientResponseException) {
                println("[OpenRouterAdviceStrategy] ❌ ERROR ${e.statusCode} con modelo '$currentModel': ${e.message}")
                println("[OpenRouterAdviceStrategy] Response body: ${e.responseBodyAsString}")

                if (e.statusCode.value() == 401) {
                    println("[OpenRouterAdviceStrategy] Error 401: API Key inválida - no intentar más modelos")
                    return generarConsejoFallback(contexto)
                }

                // Si no es el último modelo, continuar con el siguiente
                if (index < modelsToTry.size - 1) {
                    println("[OpenRouterAdviceStrategy] ⏭️ Intentando con siguiente modelo de fallback...")
                } else {
                    println("[OpenRouterAdviceStrategy] ⚠️ Todos los modelos fallaron, usando modo fallback local")
                }

            } catch (e: Exception) {
                println("[OpenRouterAdviceStrategy] ❌ ERROR con modelo '$currentModel': ${e.javaClass.simpleName}: ${e.message}")

                // Si no es el último modelo, continuar con el siguiente
                if (index < modelsToTry.size - 1) {
                    println("[OpenRouterAdviceStrategy] ⏭️ Intentando con siguiente modelo de fallback...")
                } else {
                    println("[OpenRouterAdviceStrategy] ⚠️ Todos los modelos fallaron, usando modo fallback local")
                }
            }
        }

        // Si todos los modelos fallaron, usar el modo fallback local
        println("[OpenRouterAdviceStrategy] 💡 Generando consejo con modo fallback local")
        return generarConsejoFallback(contexto)
    }

    /**
     * Genera un consejo básico sin usar la API de OpenRouter.
     * Útil como fallback cuando la API falla o no está configurada.
     */
    private fun generarConsejoFallback(contexto: String): String {
        println("[OpenRouterAdviceStrategy] Generando consejo fallback (modo local)")

        // Extraer información del contexto para generar un consejo básico
        val tareas = contexto.split("\n").filter { it.startsWith("- ") }

        val sb = StringBuilder()
        sb.append("📚 **Resumen de tus Tareas**\n\n")
        sb.append("ℹ️ *Nota: Consejo generado en modo local (OpenRouter no disponible)*\n\n")

        if (tareas.isEmpty()) {
            sb.append("No tienes tareas pendientes registradas.\n\n")
            sb.append("💡 **Sugerencias:**\n")
            sb.append("• Mantén tu planificador actualizado\n")
            sb.append("• Agrega tus próximas tareas y exámenes\n")
            sb.append("• Revisa regularmente tus materias\n")
        } else {
            sb.append("**Tienes ${tareas.size} tarea(s) pendiente(s):**\n\n")

            tareas.forEachIndexed { index, tarea ->
                val esPrioritaria = tarea.contains("vence", ignoreCase = true)
                val emoji = if (esPrioritaria) "🔴" else "📌"
                sb.append("$emoji ${tarea.removePrefix("- ")}\n")
            }

            sb.append("\n💡 **Consejos Generales:**\n")
            sb.append("• Prioriza las tareas con fechas límite cercanas\n")
            sb.append("• Dedica al menos 2 horas diarias a tareas pendientes\n")
            sb.append("• Usa la técnica Pomodoro: 25 min trabajo + 5 min descanso\n")
            sb.append("• Divide tareas grandes en subtareas más pequeñas\n")
            sb.append("• Revisa tu progreso al final del día\n")
        }

        sb.append("\n---\n")
        sb.append("⚙️ Para habilitar consejos avanzados de IA, configura la API key de OpenRouter.\n")
        sb.append("Ver: CONFIGURACION_OPENROUTER.md\n")

        return sb.toString()
    }
}
