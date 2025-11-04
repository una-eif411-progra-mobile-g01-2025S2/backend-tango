package cr.una.pai.service

import cr.una.pai.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Servicio que demuestra el uso del patrón Repository actualizado
 * con las nuevas entidades intermedias UserRole y RolePrivilege.
 */
@Service
@Transactional
class SecurityService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PrivilegeRepository,
    private val userRoleRepository: UserRoleRepository,
    private val rolePrivilegeRepository: RolePrivilegeRepository
) {

    /**
     * Obtener roles de un usuario usando la nueva entidad intermedia UserRole
     */
    fun getUserRoles(userId: UUID): List<Role> {
        return userRoleRepository.findUserRolesWithRoleByUserId(userId)
            .mapNotNull { it.role }
    }

    /**
     * Obtener privilegios de un rol usando la nueva entidad intermedia RolePrivilege
     */
    fun getRolePrivileges(roleId: UUID): List<Privilege> {
        return rolePrivilegeRepository.findRolePrivilegesWithPrivilegeByRoleId(roleId)
            .mapNotNull { it.privilege }
    }

    /**
     * Asignar rol a usuario creando la entidad intermedia UserRole
     */
    fun assignRoleToUser(userId: UUID, roleId: UUID): UserRole {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: $userId") }

        val role = roleRepository.findById(roleId)
            .orElseThrow { IllegalArgumentException("Rol no encontrado: $roleId") }

        // Verificar si ya existe la relación
        val existingUserRole = userRoleRepository.findAllByUserId(userId)
            .find { it.role?.id == roleId }

        if (existingUserRole != null) {
            throw IllegalArgumentException("El usuario ya tiene este rol asignado")
        }

        // Crear nueva relación UserRole
        val userRole = UserRole(
            id = UserRoleId(userId = userId, roleId = roleId),
            user = user,
            role = role
        )

        return userRoleRepository.save(userRole)
    }

    /**
     * Elimina la relación UserRole para quitar un rol de un usuario.
     */
    fun removeRoleFromUser(userId: UUID, roleId: UUID) {
        val userRoleId = UserRoleId(userId = userId, roleId = roleId)
        userRoleRepository.findById(userRoleId).ifPresent { userRoleRepository.delete(it) }
    }

    /**
     * Asignar privilegio a rol creando la entidad intermedia RolePrivilege
     */
    fun assignPrivilegeToRole(roleId: UUID, privilegeId: UUID): RolePrivilege {
        val role = roleRepository.findById(roleId)
            .orElseThrow { IllegalArgumentException("Rol no encontrado: $roleId") }

        val privilege = privilegeRepository.findById(privilegeId)
            .orElseThrow { IllegalArgumentException("Privilegio no encontrado: $privilegeId") }

        // Verificar si ya existe la relación
        val existingRolePrivilege = rolePrivilegeRepository.findAllByRoleId(roleId)
            .find { it.privilege?.id == privilegeId }

        if (existingRolePrivilege != null) {
            throw IllegalArgumentException("El rol ya tiene este privilegio asignado")
        }

        // Crear nueva relación RolePrivilege
        val rolePrivilege = RolePrivilege(
            id = RolePrivilegeId(roleId = roleId, privilegeId = privilegeId),
            role = role,
            privilege = privilege
        )

        return rolePrivilegeRepository.save(rolePrivilege)
    }

    /**
     * Verificar si un usuario tiene un privilegio específico
     * Demuestra consultas complejas a través de múltiples repositorios
     */
    fun userHasPrivilege(userId: UUID, privilegeName: String): Boolean {
        // Obtener roles del usuario
        val userRoles = userRoleRepository.findUserRolesWithRoleByUserId(userId)

        // Para cada rol, verificar si tiene el privilegio
        return userRoles.any { userRole ->
            userRole.role?.let { role ->
                rolePrivilegeRepository.findRolePrivilegesWithPrivilegeByRoleId(role.id!!)
                    .any { it.privilege?.name == privilegeName }
            } ?: false
        }
    }
}
