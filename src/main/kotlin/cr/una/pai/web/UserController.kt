package cr.una.pai.web

import cr.una.pai.dto.UserInput
import cr.una.pai.dto.UserResult
import cr.una.pai.dto.UserUpdateInput
import cr.una.pai.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.users}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResult>> =
        ResponseEntity.ok(userService.findAllResults())

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserResult> = try {
        ResponseEntity.ok(userService.findResultById(id))
    } catch (e: IllegalArgumentException) { ResponseEntity.notFound().build() }

    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserResult> =
        userService.findByEmail(email)
            .map { ResponseEntity.ok(userService.findResultById(it.id!!)) }
            .orElse(ResponseEntity.notFound().build())

    @PostMapping
    fun createUser(@Valid @RequestBody input: UserInput): ResponseEntity<Any> = try {
        val created = userService.create(input)
        ResponseEntity.status(HttpStatus.CREATED).body(created)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody input: UserInput): ResponseEntity<Any> = createUser(input)

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody input: UserUpdateInput
    ): ResponseEntity<Any> = try {
        val updated = userService.updateDto(id, input)
        ResponseEntity.ok(updated)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @PatchMapping("/{id}")
    fun patchUser(
        @PathVariable id: UUID,
        @Valid @RequestBody input: UserInput
    ): ResponseEntity<Any> = try {
        val updated = userService.updateFromInput(id, input)
        ResponseEntity.ok(updated)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Any> = try {
        userService.delete(id)
        ResponseEntity.noContent().build()
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }
}
