package cr.una.pai.web

import cr.una.pai.dto.AIAdvisorRequest
import cr.una.pai.dto.AIAdvisorResponse
import cr.una.pai.service.AIAdvisorService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/ai-advisor")
@CrossOrigin(origins = ["*"])
class AIAdvisorController(
    private val aiAdvisorService: AIAdvisorService
) {

    @Operation(summary = "Obtiene consejos personalizados para el usuario")
    @PostMapping("/advice")
    fun obtenerConsejo(@RequestBody request: AIAdvisorRequest): ResponseEntity<AIAdvisorResponse> {
        val response = aiAdvisorService.obtenerConsejosParaUsuario(
            userId = request.userId,
            customMessage = request.customMessage
        )
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Obtiene consejos para el usuario por su ID")
    @GetMapping("/advice/{userId}")
    fun obtenerConsejoPorUserId(@PathVariable userId: UUID): ResponseEntity<AIAdvisorResponse> {
        val response = aiAdvisorService.obtenerConsejosParaUsuario(userId = userId)
        return ResponseEntity.ok(response)
    }
}

// Controller adicional para compatibilidad con el frontend
@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = ["*"])
class AIController(
    private val aiAdvisorService: AIAdvisorService
) {

    @Operation(summary = "Obtiene consejos de estudio personalizados (ruta alternativa)")
    @GetMapping("/study-tips/{userId}")
    fun getStudyTips(@PathVariable userId: UUID): ResponseEntity<AIAdvisorResponse> {
        val response = aiAdvisorService.obtenerConsejosParaUsuario(userId = userId)
        return ResponseEntity.ok(response)
    }
}

