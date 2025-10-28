package cr.una.pai.service

import cr.una.pai.domain.RefreshToken
import cr.una.pai.domain.RefreshTokenRepository
import cr.una.pai.domain.UserRepository
import cr.una.pai.dto.AuthTokensResponse
import cr.una.pai.dto.LoginRequest
import cr.una.pai.dto.RefreshTokenRequest
import cr.una.pai.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.HexFormat
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    @Transactional
    fun login(request: LoginRequest): AuthTokensResponse {
        val email = request.email.trim().lowercase()
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, request.password)
            )
        } catch (ex: BadCredentialsException) {
            throw InvalidCredentialsException("Credenciales inválidas", ex)
        } catch (ex: AuthenticationException) {
            throw InvalidCredentialsException("Credenciales inválidas", ex)
        }

        val user = userRepository.findByEmail(email)
            .orElseThrow { InvalidCredentialsException("Credenciales inválidas") }

        val primaryRole = user.userRoles.mapNotNull { it.role?.name }.firstOrNull()
        revokeActiveRefreshTokens(user.id!!)

        val accessToken = jwtService.generateAccessToken(user, primaryRole)
        val refreshToken = jwtService.generateRefreshToken(user)
        persistRefreshToken(user.id!!, refreshToken)

        return AuthTokensResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtService.accessTokenTtl(),
            refreshTokenExpiresIn = jwtService.refreshTokenTtl()
        )
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): AuthTokensResponse {
        val claims = try {
            jwtService.parseAndValidate(request.refreshToken)
        } catch (ex: Exception) {
            throw InvalidRefreshTokenException("Refresh token inválido", ex)
        }

        if ((claims[CLAIM_TYPE] as? String) != jwtService.refreshTokenType()) {
            throw InvalidRefreshTokenException("El token recibido no es de tipo refresh")
        }

        val userId = runCatching { UUID.fromString(claims.subject) }
            .getOrElse { throw InvalidRefreshTokenException("El token no contiene un subject válido") }

        val storedToken = refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken))
            .orElseThrow { InvalidRefreshTokenException("Refresh token no registrado") }

        val now = Instant.now()
        if (storedToken.revoked || storedToken.expiresAt.isBefore(now) || storedToken.user?.id != userId) {
            throw InvalidRefreshTokenException("Refresh token inválido o expirado")
        }

        val user = storedToken.user ?: throw InvalidRefreshTokenException("Usuario no encontrado para el token")
        val primaryRole = user.userRoles.mapNotNull { it.role?.name }.firstOrNull()

        storedToken.revoked = true
        storedToken.revokedAt = now
        refreshTokenRepository.save(storedToken)

        val accessToken = jwtService.generateAccessToken(user, primaryRole)
        val newRefreshToken = jwtService.generateRefreshToken(user)
        persistRefreshToken(userId, newRefreshToken)

        return AuthTokensResponse(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = jwtService.accessTokenTtl(),
            refreshTokenExpiresIn = jwtService.refreshTokenTtl()
        )
    }

    @Transactional
    fun logout(request: RefreshTokenRequest) {
        val hash = hashToken(request.refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(hash).orElse(null) ?: return
        if (!storedToken.revoked) {
            storedToken.revoked = true
            storedToken.revokedAt = Instant.now()
            refreshTokenRepository.save(storedToken)
        }
    }

    private fun revokeActiveRefreshTokens(userId: UUID) {
        val activeTokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId)
        if (activeTokens.isEmpty()) return
        val now = Instant.now()
        activeTokens.forEach {
            it.revoked = true
            it.revokedAt = now
        }
        refreshTokenRepository.saveAll(activeTokens)
    }

    private fun persistRefreshToken(userId: UUID, refreshToken: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidRefreshTokenException("Usuario no encontrado para refresh token") }

        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusSeconds(jwtService.refreshTokenTtl())

        val token = RefreshToken(
            user = user,
            tokenHash = hashToken(refreshToken),
            issuedAt = issuedAt,
            expiresAt = expiresAt
        )
        refreshTokenRepository.save(token)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray())
        return HexFormat.of().formatHex(hash)
    }

    class InvalidCredentialsException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
    class InvalidRefreshTokenException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

    companion object {
        private const val CLAIM_TYPE = "typ"
    }
}
