package cr.una.pai.security

import cr.una.pai.domain.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) {

    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(val email: String, val accessToken: String, val refreshToken: String)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )

            val user = userRepository.findByEmail(request.email)
                .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

            val accessToken = jwtService.generateAccessToken(user, user.userRoles.firstOrNull()?.role?.name)
            val refreshToken = jwtService.generateRefreshToken(user)

            ResponseEntity.ok(LoginResponse(request.email, accessToken, refreshToken))
        } catch (ex: AuthenticationException) {
            ResponseEntity.status(401).body(mapOf("error" to "Credenciales inválidas"))
        }
    }
}
