package cr.una.pai.web

import cr.una.pai.dto.PrivilegeResult
import cr.una.pai.dto.RoleResult
import cr.una.pai.mapper.PrivilegeMapper
import cr.una.pai.mapper.RoleMapper
import cr.una.pai.service.SecurityService
import io.swagger.v3.oas.annotations.Operation
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

    @Operation(summary = "Obtiene los roles asignados a un usuario")
    @GetMapping("/user/{userId}/roles")
    fun getUserRoles(@PathVariable userId: UUID): ResponseEntity<List<RoleResult>> =
        ResponseEntity.ok(securityService.getUserRoles(userId).map(roleMapper::toResult))

    @Operation(summary = "Obtiene los privilegios asignados a un rol")
    @GetMapping("/role/{roleId}/privileges")
    fun getRolePrivileges(@PathVariable roleId: UUID): ResponseEntity<List<PrivilegeResult>> =
        ResponseEntity.ok(securityService.getRolePrivileges(roleId).map(privilegeMapper::toResult))

    @Operation(summary = "Asigna un rol a un usuario")
    @PostMapping("/user/{userId}/role/{roleId}")
    fun assignRoleToUser(
        @PathVariable userId: UUID,
        @PathVariable roleId: UUID
    ): ResponseEntity<Map<String, String>> {
        securityService.assignRoleToUser(userId, roleId)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("message" to "Rol asignado correctamente"))
    }

    @Operation(summary = "Elimina un rol asignado a un usuario")
    @DeleteMapping("/user/{userId}/role/{roleId}")
    fun removeRoleFromUser(
        @PathVariable userId: UUID,
        @PathVariable roleId: UUID
    ): ResponseEntity<Void> {
        securityService.removeRoleFromUser(userId, roleId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Asigna un privilegio a un rol")
    @PostMapping("/role/{roleId}/privilege/{privilegeId}")
    fun assignPrivilegeToRole(
        @PathVariable roleId: UUID,
        @PathVariable privilegeId: UUID
    ): ResponseEntity<Void> {
        securityService.assignPrivilegeToRole(roleId, privilegeId)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @Operation(summary = "Verifica si un usuario tiene un privilegio específico")
    @GetMapping("/user/{userId}/has-privilege/{privilegeName}")
    fun userHasPrivilege(
        @PathVariable userId: UUID,
        @PathVariable privilegeName: String
    ): ResponseEntity<Map<String, Boolean>> {
        val hasPrivilege = securityService.userHasPrivilege(userId, privilegeName)
        return ResponseEntity.ok(mapOf("hasPrivilege" to hasPrivilege))
    }
}
