package cr.una.pai.web

import cr.una.pai.dto.PrivilegeResult
import cr.una.pai.dto.RoleResult
import cr.una.pai.mapper.PrivilegeMapper
import cr.una.pai.mapper.RoleMapper
import cr.una.pai.service.SecurityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.roles}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class SecurityController(
    private val securityService: SecurityService,
    private val roleMapper: RoleMapper,
    private val privilegeMapper: PrivilegeMapper
) {

    @GetMapping("/user/{userId}/roles")
    fun getUserRoles(@PathVariable userId: UUID): ResponseEntity<List<RoleResult>> =
        ResponseEntity.ok(securityService.getUserRoles(userId).map(roleMapper::toResult))

    @GetMapping("/role/{roleId}/privileges")
    fun getRolePrivileges(@PathVariable roleId: UUID): ResponseEntity<List<PrivilegeResult>> =
        ResponseEntity.ok(securityService.getRolePrivileges(roleId).map(privilegeMapper::toResult))

    @PostMapping("/user/{userId}/role/{roleId}")
    fun assignRoleToUser(
        @PathVariable userId: UUID,
        @PathVariable roleId: UUID
    ): ResponseEntity<Any> = try {
        securityService.assignRoleToUser(userId, roleId)
        ResponseEntity.status(HttpStatus.CREATED).build()
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @PostMapping("/role/{roleId}/privilege/{privilegeId}")
    fun assignPrivilegeToRole(
        @PathVariable roleId: UUID,
        @PathVariable privilegeId: UUID
    ): ResponseEntity<Any> = try {
        securityService.assignPrivilegeToRole(roleId, privilegeId)
        ResponseEntity.status(HttpStatus.CREATED).build()
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
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
