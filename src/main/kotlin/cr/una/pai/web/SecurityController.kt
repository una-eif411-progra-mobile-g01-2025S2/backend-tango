package cr.una.pai.web

import cr.una.pai.domain.*
import cr.una.pai.service.SecurityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.roles}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class SecurityController(
    private val securityService: SecurityService
) {

    @GetMapping("/user/{userId}/roles")
    fun getUserRoles(@PathVariable userId: UUID): ResponseEntity<List<Role>> {
        return ResponseEntity.ok(securityService.getUserRoles(userId))
    }

    @GetMapping("/role/{roleId}/privileges")
    fun getRolePrivileges(@PathVariable roleId: UUID): ResponseEntity<List<Privilege>> {
        return ResponseEntity.ok(securityService.getRolePrivileges(roleId))
    }

    @PostMapping("/user/{userId}/role/{roleId}")
    fun assignRoleToUser(
        @PathVariable userId: UUID,
        @PathVariable roleId: UUID
    ): ResponseEntity<Any> {
        return try {
            val userRole = securityService.assignRoleToUser(userId, roleId)
            ResponseEntity.status(HttpStatus.CREATED).body(userRole)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/role/{roleId}/privilege/{privilegeId}")
    fun assignPrivilegeToRole(
        @PathVariable roleId: UUID,
        @PathVariable privilegeId: UUID
    ): ResponseEntity<Any> {
        return try {
            val rolePrivilege = securityService.assignPrivilegeToRole(roleId, privilegeId)
            ResponseEntity.status(HttpStatus.CREATED).body(rolePrivilege)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/user/{userId}/has-privilege/{privilegeName}")
    fun userHasPrivilege(
        @PathVariable userId: UUID,
        @PathVariable privilegeName: String
    ): ResponseEntity<Map<String, Boolean>> {
        val hasPrivilege = securityService.userHasPrivilege(userId, privilegeName)
        return ResponseEntity.ok(mapOf("hasPrivilege" to hasPrivilege))
    }
}
