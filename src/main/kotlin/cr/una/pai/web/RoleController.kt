package cr.una.pai.web

import cr.una.pai.domain.Role
import cr.una.pai.domain.RoleRepository
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/roles")
@CrossOrigin(origins = ["*"])
class RoleController(
    private val roleRepository: RoleRepository
) {

    @Operation(summary = "Obtiene todos los roles disponibles")
    @GetMapping
    fun getAllRoles(): ResponseEntity<List<Role>> =
        ResponseEntity.ok(roleRepository.findAll())

    @Operation(summary = "Obtiene el rol USER por defecto")
    @GetMapping("/default")
    fun getDefaultRole(): ResponseEntity<Role> {
        val userRole = roleRepository.findByName("USER")
        return if (userRole.isPresent) {
            ResponseEntity.ok(userRole.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

