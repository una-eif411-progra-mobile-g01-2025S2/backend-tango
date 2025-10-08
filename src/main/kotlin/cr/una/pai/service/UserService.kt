package cr.una.pai.service

import cr.una.pai.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {

    fun findAll(): List<User> = userRepository.findAll()

    fun findById(id: UUID): Optional<User> = userRepository.findById(id)

    fun findByEmail(email: String): Optional<User> = userRepository.findByEmail(email)

    fun create(user: User): User {
        // Validar que el email no esté en uso
        if (userRepository.findByEmail(user.email).isPresent) {
            throw IllegalArgumentException("El email ${user.email} ya está registrado")
        }

        return userRepository.save(user)
    }

    fun update(id: UUID, user: User): User {
        val existing = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: $id") }

        // Validar email único si cambió
        if (existing.email != user.email && userRepository.findByEmail(user.email).isPresent) {
            throw IllegalArgumentException("El email ${user.email} ya está en uso")
        }

        existing.email = user.email
        existing.fullName = user.fullName
        existing.password = user.password
        existing.degree = user.degree
        existing.yearOfStudy = user.yearOfStudy
        existing.university = user.university

        return userRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw IllegalArgumentException("Usuario no encontrado: $id")
        }
        userRepository.deleteById(id)
    }
}

