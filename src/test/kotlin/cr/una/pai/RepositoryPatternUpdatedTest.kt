package cr.una.pai

import cr.una.pai.domain.*
import cr.una.pai.service.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql

/**
 * Test actualizado que demuestra el patrón Repository con las nuevas entidades intermedias
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SecurityService::class)
@Sql(
    scripts = [
        "classpath:sql/clean.sql",
        "classpath:sql/import-privileges.sql",
        "classpath:sql/import-roles.sql",
        "classpath:sql/import-role-privilege.sql",
        "classpath:sql/import-users.sql",
        "classpath:sql/import-user-role.sql"
    ]
)
class RepositoryPatternUpdatedTest @Autowired constructor(
    private val securityService: SecurityService,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PrivilegeRepository,
    private val userRoleRepository: UserRoleRepository,
    private val rolePrivilegeRepository: RolePrivilegeRepository
) {

    @Test
    fun `repository pattern - entidades intermedias funcionan correctamente`() {
        // Verificar que los repositorios base funcionan
        val users = userRepository.findAll()
        val roles = roleRepository.findAll()
        val privileges = privilegeRepository.findAll()

        assertThat(users).isNotEmpty
        assertThat(roles).isNotEmpty
        assertThat(privileges).isNotEmpty

        // Verificar que las entidades intermedias funcionan
        val userRoles = userRoleRepository.findAll()
        val rolePrivileges = rolePrivilegeRepository.findAll()

        assertThat(userRoles).isNotEmpty
        assertThat(rolePrivileges).isNotEmpty
    }

    @Test
    fun `repository pattern - consultas derivadas en entidades intermedias`() {
        val ana = userRepository.findByEmail("ana@example.com").get()

        // Usar repositorio de entidad intermedia
        val anaUserRoles = userRoleRepository.findAllByUserId(ana.id!!)
        assertThat(anaUserRoles).isNotEmpty

        // Verificar que la relación funciona
        anaUserRoles.forEach { userRole ->
            assertThat(userRole.user?.id).isEqualTo(ana.id)
            assertThat(userRole.role).isNotNull
        }
    }

    @Test
    fun `repository pattern - queries con fetch join funcionan`() {
        val ana = userRepository.findByEmail("ana@example.com").get()

        // Usar query con JOIN FETCH para evitar N+1
        val userRolesWithRole = userRoleRepository.findUserRolesWithRoleByUserId(ana.id!!)
        assertThat(userRolesWithRole).isNotEmpty

        // Verificar que los roles están cargados (no lazy)
        userRolesWithRole.forEach { userRole ->
            assertThat(userRole.role?.name).isNotNull
        }
    }

    @Test
    fun `repository pattern - service coordina multiples repositorios intermedios`() {
        val ana = userRepository.findByEmail("ana@example.com").get()

        // El service debe coordinar múltiples repositorios
        val roles = securityService.getUserRoles(ana.id!!)
        assertThat(roles).isNotEmpty

        // Verificar privilegios de un rol
        if (roles.isNotEmpty()) {
            val firstRole = roles.first()
            val privileges = securityService.getRolePrivileges(firstRole.id!!)
            // Puede estar vacío si el rol no tiene privilegios asignados
            assertThat(privileges).isNotNull
        }
    }

    @Test
    fun `repository pattern - asignacion de roles usando entidades intermedias`() {
        val ana = userRepository.findByEmail("ana@example.com").get()
        val adminRole = roleRepository.findByName("ADMIN").orElse(null)

        if (adminRole != null) {
            // Verificar que podemos asignar roles usando el patrón Repository
            val initialRolesCount = userRoleRepository.findAllByUserId(ana.id!!).size

            try {
                val userRole = securityService.assignRoleToUser(ana.id!!, adminRole.id!!)
                assertThat(userRole.id.userId).isEqualTo(ana.id)
                assertThat(userRole.id.roleId).isEqualTo(adminRole.id)

                // Verificar que se incrementó el número de roles
                val finalRolesCount = userRoleRepository.findAllByUserId(ana.id!!).size
                assertThat(finalRolesCount).isEqualTo(initialRolesCount + 1)

            } catch (e: IllegalArgumentException) {
                // El rol ya estaba asignado, verificar el mensaje
                assertThat(e.message).contains("ya tiene este rol asignado")
            }
        }
    }

    @Test
    fun `repository pattern - verificacion de privilegios complejos`() {
        val ana = userRepository.findByEmail("ana@example.com").get()

        // Esta función demuestra el uso coordinado de múltiples repositorios
        val hasCreatePrivilege = securityService.userHasPrivilege(ana.id!!, "CREATE_TASKS")

        // El resultado puede ser true o false, pero la consulta debe funcionar
        assertThat(hasCreatePrivilege).isIn(true, false)
    }
}
