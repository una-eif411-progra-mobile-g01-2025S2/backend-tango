package cr.una.pai.web

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${api.endpoints.ping}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class PingController {
    @Operation(summary = "Verifica el estado del servicio")
    @GetMapping
    fun ping() = mapOf("status" to "ok", "message" to "Service is running")
}
