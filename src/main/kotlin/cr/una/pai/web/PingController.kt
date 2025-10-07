package cr.una.pai.web

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${api.endpoints.ping}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class PingController {
    @GetMapping
    fun ping() = mapOf("status" to "ok", "message" to "Service is running")
}
