package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.UserInput
import cr.una.pai.dto.UserResult
import cr.una.pai.dto.UserUpdateInput
import cr.una.pai.mapper.UserMapper
import cr.una.pai.mapper.MappingContext
import cr.una.pai.mapper.mapWith
import cr.una.pai.mapper.toResults
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userMapper: UserMapper,
    private val mappingContext: MappingContext,
    private val passwordEncoder: PasswordEncoder
) {

    fun findAll(): List<User> = userRepository.findAll()
    fun findById(id: UUID): Optional<User> = userRepository.findById(id)
    fun findByEmail(email: String): Optional<User> = userRepository.findByEmail(email)

    fun create(user: User): User {
        if (userRepository.findByEmail(user.email).isPresent) {
            throw IllegalArgumentException("El email ${user.email} ya está registrado")
        }
        user.password = encodePassword(user.password)
        return userRepository.save(user)
    }

    fun update(id: UUID, user: User): User {
        val existing = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: $id") }

        if (existing.email != user.email && userRepository.findByEmail(user.email).isPresent) {
            throw IllegalArgumentException("El email ${user.email} ya está en uso")
        }

        existing.email = user.email
        existing.fullName = user.fullName
        user.password.takeUnless { it.isNullOrBlank() }?.let {
            existing.password = encodePassword(it)
        }
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

    // ================= DTO + Mapper =================
    fun create(input: UserInput): UserResult {
        if (input.email.isNullOrBlank() || input.fullName.isNullOrBlank() || input.password.isNullOrBlank())
            throw IllegalArgumentException("email, fullName y password son obligatorios")
        if (userRepository.findByEmail(input.email!!).isPresent)
            throw IllegalArgumentException("El email ${input.email} ya está registrado")

        // Si no se proporcionan roleIds o los roleIds no existen, asignar rol USER por defecto
        if (input.roleIds.isNullOrEmpty()) {
            val userRole = roleRepository.findByName("USER")
            if (userRole.isPresent) {
                input.roleIds = listOf(userRole.get().id!!)
            }
        } else {
            // Verificar que los roles existan, si no, usar USER por defecto
            val validRoleIds = input.roleIds!!.mapNotNull { roleId ->
                try {
                    roleRepository.findById(roleId).orElse(null)?.id
                } catch (e: Exception) {
                    null
                }
            }

            if (validRoleIds.isEmpty()) {
                val userRole = roleRepository.findByName("USER")
                if (userRole.isPresent) {
                    input.roleIds = listOf(userRole.get().id!!)
                }
            } else {
                input.roleIds = validRoleIds
            }
        }

        val entity = userMapper.toEntity(input, mappingContext)
        entity.password = encodePassword(entity.password)
        val saved = userRepository.save(entity)
        return userMapper.toResult(saved)
    }

    fun updateDto(id: UUID, input: UserUpdateInput): UserResult {
        val entity = userRepository.findById(id).orElseThrow { IllegalArgumentException("Usuario no encontrado: $id") }
        // email se ignora en mapper (inmutable aquí); si se desea permitir cambio debe hacerse validación manual
        userMapper.update(entity, input, mappingContext)
        if (!input.password.isNullOrBlank()) {
            entity.password = encodePassword(input.password!!)
        }
        val saved = userRepository.save(entity)
        return userMapper.toResult(saved)
    }

    fun updateFromInput(id: UUID, input: UserInput): UserResult {
        val entity = userRepository.findById(id).orElseThrow { IllegalArgumentException("Usuario no encontrado: $id") }
        // No se permite cambio de email en este flujo (mapper ignora)
        userMapper.updateFromInput(entity, input, mappingContext)
        if (!input.password.isNullOrBlank()) {
            entity.password = encodePassword(input.password!!)
        }
        val saved = userRepository.save(entity)
        return userMapper.toResult(saved)
    }

    fun findResultById(id: UUID): UserResult = userMapper.toResult(
        userRepository.findById(id).orElseThrow { IllegalArgumentException("Usuario no encontrado: $id") }
    )

    fun findAllResults(): List<UserResult> = userRepository.findAll().toResults(userMapper::toResult)

    fun findAllPaged(pageable: Pageable): Page<UserResult> = userRepository.findAll(pageable).mapWith(userMapper::toResult)

    private fun encodePassword(raw: String): String {
        val password = raw.trim()
        if (password.isEmpty()) {
            throw IllegalArgumentException("Password no puede estar vacío")
        }
        return if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
            password
        } else {
            passwordEncoder.encode(password)
        }
    }
}
