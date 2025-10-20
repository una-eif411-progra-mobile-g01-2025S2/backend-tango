package cr.una.pai.web

import cr.una.pai.dto.AIAdvisorRequest
import cr.una.pai.dto.AIAdvisorResponse
import cr.una.pai.service.AIAdvisorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/ai-advisor")
@CrossOrigin(origins = ["*"])
class AIAdvisorController(
    private val aiAdvisorService: AIAdvisorService
) {

    @PostMapping("/advice")
    fun obtenerConsejo(@RequestBody request: AIAdvisorRequest): ResponseEntity<AIAdvisorResponse> {
        val response = aiAdvisorService.obtenerConsejosParaUsuario(
            userId = request.userId,
            customMessage = request.customMessage
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/advice/{userId}")
    fun obtenerConsejoPorUserId(@PathVariable userId: UUID): ResponseEntity<AIAdvisorResponse> {
        val response = aiAdvisorService.obtenerConsejosParaUsuario(userId = userId)
        return ResponseEntity.ok(response)
    }
}

