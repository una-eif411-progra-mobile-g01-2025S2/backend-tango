package cr.una.pai.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class LoginRequest(
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "Email es requerido")
    val email: String,
    @field:NotBlank(message = "Password es requerido")
    val password: String
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "El refresh token es requerido")
    val refreshToken: String
)

data class AuthTokensResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)

data class AuthUser(
    val id: UUID,
    val name: String,
    val email: String
)

data class LoginResponse(
    val token: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String? = null,
    val expiresIn: Long,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long? = null,
    val user: AuthUser,
    val userDto: AuthUser
)
