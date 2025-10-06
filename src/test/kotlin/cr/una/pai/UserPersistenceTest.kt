package cr.una.pai

import cr.una.pai.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.jdbc.Sql

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = [
        "classpath:sql/clean.sql",
    ]
)
@Rollback(false)
class UserPersistenceTest @Autowired constructor(
        val userRepository: UserRepository
) {

    @Test
    fun `crear un usuario nuevo`() {
        val newUser = User(
                email = "usuario@example.com",
                password = "12345",
                fullName = "Jerry",
                degree = "Ingeniería en Sistemas",
                yearOfStudy = 6,
                university = "Campus Benjamín"
        )

        val savedUser = userRepository.save(newUser)

        assertThat(savedUser.id).isNotNull()
        assertThat(savedUser.email).isEqualTo("usuario@example.com")
        println("Usuario creado correctamente: ${savedUser.fullName} (${savedUser.email})")
    }

    @Test
    fun `actualizar datos de usuario existente`() {
        // Crear un usuario si no existe
        val existingUser = userRepository.findByEmail("usuario@example.com").orElseGet {
            userRepository.save(
                User(
                    email = "usuario@example.com",
                    password = "12345",
                    fullName = "Jerry",
                    degree = "Ingeniería en Sistemas",
                    yearOfStudy = 6,
                    university = "Campus Benjamín"
                )
            )
        }

        existingUser.fullName = "Usuario Actualizado"
        existingUser.yearOfStudy = 4
        val updatedUser = userRepository.save(existingUser)

        assertThat(updatedUser.fullName).isEqualTo("Usuario Actualizado")
        assertThat(updatedUser.yearOfStudy).isEqualTo(4)
        println("Usuario actualizado correctamente: ${updatedUser.fullName}")
    }
}
