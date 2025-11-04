package cr.una.pai.service

import cr.una.pai.domain.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(SecurityService::class)
class SecurityServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PrivilegeRepository,
    private val securityService: SecurityService,
    private val userRoleRepository: UserRoleRepository,
    private val rolePrivilegeRepository: RolePrivilegeRepository
) {

    @Test
    fun `assignRoleToUser prevents duplicates`() {
        val user = userRepository.save(
            User(
                email = "duplicate@test.com",
                password = "secret",
                fullName = "Duplicate Tester"
            )
        )
        val role = roleRepository.save(
            Role(
                name = "TEST_ROLE"
            )
        )

        securityService.assignRoleToUser(user.id!!, role.id!!)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            securityService.assignRoleToUser(user.id!!, role.id!!)
        }
        assertEquals("El usuario ya tiene este rol asignado", exception.message)
    }

    @Test
    fun `removeRoleFromUser is idempotent`() {
        val user = userRepository.save(
            User(
                email = "idempotent@test.com",
                password = "secret",
                fullName = "Idempotent Tester"
            )
        )
        val role = roleRepository.save(Role(name = "IDEMPOTENT_ROLE"))

        // Assign once and delete
        securityService.assignRoleToUser(user.id!!, role.id!!)
        securityService.removeRoleFromUser(user.id!!, role.id!!)

        // Call delete again - should not raise an exception
        assertDoesNotThrow {
            securityService.removeRoleFromUser(user.id!!, role.id!!)
        }
        assertEquals(0, userRoleRepository.findAllByUserId(user.id!!).size)
    }

    @Test
    fun `assignPrivilegeToRole prevents duplicates`() {
        val role = roleRepository.save(Role(name = "PRIV_ROLE"))
        val privilege = privilegeRepository.save(Privilege(name = "CAN_TEST"))

        securityService.assignPrivilegeToRole(role.id!!, privilege.id!!)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            securityService.assignPrivilegeToRole(role.id!!, privilege.id!!)
        }
        assertEquals("El rol ya tiene este privilegio asignado", exception.message)
        assertEquals(1, rolePrivilegeRepository.findAllByRoleId(role.id!!).size)
    }
}
