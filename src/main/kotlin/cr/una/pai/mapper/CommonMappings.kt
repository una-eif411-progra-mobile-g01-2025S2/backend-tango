package cr.una.pai.mapper

import cr.una.pai.domain.RolePrivilege
import cr.una.pai.domain.UserRole
import cr.una.pai.dto.PrivilegeResult
import cr.una.pai.dto.RoleResult
import org.mapstruct.Named
import org.springframework.data.domain.Page

class CommonMappings {
    companion object {
        @Named("rolePrivilegesToPrivilegeResults")
        @JvmStatic
        fun rolePrivilegesToPrivilegeResults(source: MutableSet<RolePrivilege>?): List<PrivilegeResult> =
            source?.mapNotNull { rp -> rp.privilege?.let { PrivilegeResult(it.id!!, it.name, it.description) } } ?: emptyList()

        @Named("userRolesToRoleResults")
        @JvmStatic
        fun userRolesToRoleResults(source: MutableSet<UserRole>?): List<RoleResult> =
            source?.mapNotNull { ur ->
                ur.role?.let { r ->
                    val privileges = rolePrivilegesToPrivilegeResults(r.rolePrivileges)
                    RoleResult(r.id!!, r.name, r.description, privileges)
                }
            } ?: emptyList()
    }
}

// =====================
// Extensiones genéricas
// =====================
/**
 * Convierte un Page<E> a Page<R> aplicando el mapper provisto.
 */
fun <E, R> Page<E>.mapWith(mapper: (E) -> R): Page<R> = this.map { mapper(it) }

/**
 * Convierte un Iterable<E> en una lista de R aplicando el mapper.
 */
fun <E, R> Iterable<E>.toResults(mapper: (E) -> R): List<R> = this.map { mapper(it) }
